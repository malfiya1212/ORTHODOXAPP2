using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace EOC.Finance.API.Models
{
    public class Payment
    {
        [Key]
        public Guid Id { get; set; } = Guid.NewGuid();

        [Required]
        public string PaymentType { get; set; } = string.Empty; 
        // Tithe | Donation | Offering | BuildingFund | DiasporaSupport

        [Required]
        [Column(TypeName = "decimal(18,2)")]
        public decimal Amount { get; set; } // The base amount converted to ETB

        [Required]
        [Column(TypeName = "decimal(18,2)")]
        public decimal OriginalAmount { get; set; } // Amount in original currency (e.g., 100.00)

        [Required]
        public string CurrencyCode { get; set; } = "ETB"; // USD | ETB | EUR | GBP

        [Column(TypeName = "decimal(18,4)")]
        public decimal ExchangeRate { get; set; } // The rate used at time of transaction

        [Required]
        public DateTime PaymentDate { get; set; } = DateTime.UtcNow;

        [Required]
        public long ChurchId { get; set; }

        public long? MemberId { get; set; }

        [Required]
        public string PaymentMethod { get; set; } = string.Empty; 
        // Cash | Telebirr | BankTransfer | PayPal | Stripe | Offline

        public string? ReferenceNumber { get; set; } // Bank ref or Mobile Money transaction ID

        public string Status { get; set; } = "Completed"; 
        // Pending | Completed | Synced | Failed | UnderAudit

        public string ReceiptNumber { get; set; } = string.Empty;

        [Required]
        public long CreatedBy { get; set; }
    }
}
