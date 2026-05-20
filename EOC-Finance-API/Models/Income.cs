using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace EOC.Finance.API.Models
{
    public class Income
    {
        [Key]
        public long Id { get; set; }

        [Required]
        public double Amount { get; set; }

        [Required]
        public string Source { get; set; } = string.Empty;

        [Required]
        public string Category { get; set; } = "Tithe"; // አስራት

        [Required]
        public DateTime Date { get; set; } = DateTime.UtcNow;

        [Required]
        public string Status { get; set; } = "PENDING";

        [Required]
        public long ChurchId { get; set; }

        [ForeignKey("ChurchId")]
        public Church? Church { get; set; }

        public long CreatedBy { get; set; }

        [ForeignKey("CreatedBy")]
        public User? Creator { get; set; }
    }
}
