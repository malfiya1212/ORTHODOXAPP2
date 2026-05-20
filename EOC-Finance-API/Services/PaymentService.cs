using EOC.Finance.API.Data;
using EOC.Finance.API.Models;
using EOC.Finance.API.DTOs;
using Microsoft.EntityFrameworkCore;
using System;
using System.Threading.Tasks;

namespace EOC.Finance.API.Services
{
    public interface IPaymentService
    {
        Task<PaymentReceiptDto> ProcessPaymentAsync(PaymentCreateDto dto, long currentUserId);
    }

    public class PaymentService : IPaymentService
    {
        private readonly AppDbContext _context;

        public PaymentService(AppDbContext context)
        {
            _context = context;
        }

        public async Task<PaymentReceiptDto> ProcessPaymentAsync(PaymentCreateDto dto, long currentUserId)
        {
            // 1. Fetch Exchange Rate & Calculate Base Amount
            decimal exchangeRate = 1.0m;
            if (dto.CurrencyCode != "ETB")
            {
                var rateRecord = await _context.Set<CurrencyRate>().FindAsync(dto.CurrencyCode);
                exchangeRate = rateRecord?.RateToEtb ?? 1.0m;
            }
            
            decimal finalEtbValue = dto.OriginalAmount * exchangeRate;

            // 2. Generate Unique Receipt Number
            string receiptNo = $"RCPT-{DateTime.UtcNow:yyyyMMdd}-{Guid.NewGuid().ToString().Substring(0, 8).ToUpper()}";

            var payment = new Payment
            {
                Id = Guid.NewGuid(),
                PaymentType = dto.PaymentType,
                OriginalAmount = dto.OriginalAmount,
                Amount = finalEtbValue, // Final ETB Value = OriginalAmount * ExchangeRate
                CurrencyCode = dto.CurrencyCode,
                ExchangeRate = exchangeRate,
                PaymentDate = DateTime.UtcNow,
                ChurchId = dto.ChurchId,
                MemberId = dto.MemberId,
                PaymentMethod = dto.PaymentMethod,
                ReferenceNumber = dto.ReferenceNumber,
                Status = dto.Status,
                CreatedBy = currentUserId,
                ReceiptNumber = receiptNo
            };

            // 2. Add Payment to Database
            _context.Payments.Add(payment);

            // 3. Create Audit Log for immutability
            var audit = new AuditLog
            {
                UserId = currentUserId,
                Action = "PAYMENT_PROCESSED",
                TableName = "Payments",
                RecordId = payment.ChurchId, // Using ChurchId as general ref since RecordId is long and Payment.Id is Guid
                Details = $"Received {finalEtbValue} ETB via {dto.PaymentMethod}. Receipt: {receiptNo}",
                Timestamp = DateTime.UtcNow
            };
            _context.AuditLogs.Add(audit);

            // 4. Update Income Reports Automatically (Bridge to existing Income system)
            var income = new Income
            {
                ChurchId = dto.ChurchId,
                Amount = (double)finalEtbValue, // Unified ETB for Reporting
                Source = dto.PaymentMethod,
                Category = dto.PaymentType,
                Status = "APPROVED", 
                CreatedBy = currentUserId,
                Date = DateTime.UtcNow
            };
            _context.IncomeRecords.Add(income);

            await _context.SaveChangesAsync();

            // 5. Generate Receipt DTO
            var church = await _context.Churches.FindAsync(dto.ChurchId);
            var member = dto.MemberId.HasValue ? await _context.Users.FindAsync(dto.MemberId.Value) : null;
            var creator = await _context.Users.FindAsync(currentUserId);

            var baseUrl = "https://api.tewahedo-finance.org"; // Production base URL
            var verificationUrl = $"{baseUrl}/api/payment/verify/{payment.ReceiptNumber}";

            return new PaymentReceiptDto
            {
                PaymentId = payment.Id,
                ReceiptNumber = payment.ReceiptNumber,
                ChurchName = church?.Name ?? "Unknown Church",
                MemberName = member?.Name,
                Amount = payment.Amount,
                PaymentType = payment.PaymentType,
                PaymentMethod = payment.PaymentMethod,
                ReferenceNumber = payment.ReferenceNumber,
                PaymentDate = payment.PaymentDate,
                GeneratedBy = creator?.Name ?? "System",
                VerificationUrl = verificationUrl
            };
        }
    }
}
