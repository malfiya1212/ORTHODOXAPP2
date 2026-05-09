using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace EOC.Finance.API.Models
{
    public class ChurchAsset
    {
        [Key]
        public Guid Id { get; set; } = Guid.NewGuid();

        [Required]
        public long ChurchId { get; set; }
        [ForeignKey("ChurchId")]
        public Church? Church { get; set; }

        [Required]
        public string AssetType { get; set; } = string.Empty; // Building | Vehicle | Equipment | Land

        [Required]
        public string Name { get; set; } = string.Empty;

        public string? Description { get; set; }

        [Column(TypeName = "decimal(18,2)")]
        public decimal PurchaseValue { get; set; }

        [Column(TypeName = "decimal(18,2)")]
        public decimal CurrentEstimatedValue { get; set; }

        public DateTime AcquisitionDate { get; set; }

        public DateTime? LastMaintenanceDate { get; set; }

        public string Status { get; set; } = "ACTIVE"; // ACTIVE | MAINTENANCE | SOLD | SCRAPPED
    }
}
