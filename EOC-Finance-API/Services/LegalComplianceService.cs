using EOC.Finance.API.Data;
using Microsoft.EntityFrameworkCore;
using System;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace EOC.Finance.API.Services
{
    public interface ILegalComplianceService
    {
        Task<string> GenerateGovernmentAuditCsvAsync(long dioceseId, int year);
    }

    public class LegalComplianceService : ILegalComplianceService
    {
        private readonly AppDbContext _context;

        public LegalComplianceService(AppDbContext context)
        {
            _context = context;
        }

        public async Task<string> GenerateGovernmentAuditCsvAsync(long dioceseId, int year)
        {
            // Gather all final approved income and expenses for a strict external audit
            var startDate = new DateTime(year, 1, 1).ToUniversalTime();
            var endDate = new DateTime(year, 12, 31, 23, 59, 59).ToUniversalTime();

            var churches = await _context.Churches.Where(c => c.DioceseId == dioceseId).Select(c => c.Id).ToListAsync();

            var incomeRecords = await _context.IncomeRecords
                .Where(i => churches.Contains(i.ChurchId) && i.Date >= startDate && i.Date <= endDate && i.Status == "APPROVED")
                .ToListAsync();

            var expenseRecords = await _context.Expenses
                .Where(e => churches.Contains(e.ChurchId) && e.Date >= startDate && e.Date <= endDate && e.Status == "FINAL_APPROVED")
                .ToListAsync();

            // Constructing an IFRS/GAAP compliant CSV format
            var csvBuilder = new StringBuilder();
            csvBuilder.AppendLine("TransactionDate,ReferenceType,ReferenceID,AccountingCategory,Debit_ETB,Credit_ETB,Net_ETB,ChurchID,AuditStatus");

            foreach (var income in incomeRecords)
            {
                csvBuilder.AppendLine($"{income.Date:yyyy-MM-dd},INCOME,{income.Id},{income.Category},0.00,{income.Amount},{income.Amount},{income.ChurchId},VERIFIED");
            }

            foreach (var expense in expenseRecords)
            {
                csvBuilder.AppendLine($"{expense.Date:yyyy-MM-dd},EXPENSE,{expense.Id},{expense.Category},{expense.Amount},0.00,-{expense.Amount},{expense.ChurchId},VERIFIED");
            }

            return csvBuilder.ToString();
        }
    }
}
