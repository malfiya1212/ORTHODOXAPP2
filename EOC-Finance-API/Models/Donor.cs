using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace EOC.Finance.API.Models
{
    public class Donor
    {
        [Key]
        public Guid Id { get; set; } = Guid.NewGuid();

        [Required]
        public string Name { get; set; } = string.Empty; // "Anonymous" if hidden

        public string? Email { get; set; }
        public string? Phone { get; set; }

        public bool IsAnonymous { get; set; } = false;

        [Column(TypeName = "decimal(18,2)")]
        public decimal TotalDonatedAmount { get; set; } = 0;

        public DateTime FirstDonationDate { get; set; } = DateTime.UtcNow;
        public DateTime LastDonationDate { get; set; } = DateTime.UtcNow;

        [Required]
        public long ChurchId { get; set; }
        [ForeignKey("ChurchId")]
        public Church? Church { get; set; }
        
        // Tier system: e.g., Standard, Silver, Gold, Platinum based on total donations
        public string DonorTier { get; set; } = "Standard";
    }
}
