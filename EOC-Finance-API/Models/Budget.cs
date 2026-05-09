using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace EOC.Finance.API.Models
{
    public class Budget
    {
        [Key]
        public long Id { get; set; }

        [Required]
        public long ChurchId { get; set; }
        [ForeignKey("ChurchId")]
        public Church? Church { get; set; }

        [Required]
        public string Category { get; set; } = string.Empty;

        [Required]
        [Column(TypeName = "decimal(18,2)")]
        public decimal Amount { get; set; }

        [Required]
        public string Period { get; set; } = "MONTHLY"; // MONTHLY, YEARLY

        public DateTime StartDate { get; set; }
        public DateTime EndDate { get; set; }
    }
}
