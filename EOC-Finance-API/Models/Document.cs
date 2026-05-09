using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace EOC.Finance.API.Models
{
    public class Document
    {
        [Key]
        public Guid Id { get; set; } = Guid.NewGuid();

        [Required]
        public string FileName { get; set; } = string.Empty;

        [Required]
        public string FileType { get; set; } = string.Empty; // Receipt | Contract | AnnualReport | DonationRecord

        [Required]
        public string BlobStorageUrl { get; set; } = string.Empty;

        [Required]
        public long ChurchId { get; set; }
        [ForeignKey("ChurchId")]
        public Church? Church { get; set; }

        public long? UploadedBy { get; set; }
        [ForeignKey("UploadedBy")]
        public User? Uploader { get; set; }

        public DateTime UploadedAt { get; set; } = DateTime.UtcNow;

        public int Version { get; set; } = 1;

        public string? ReferenceType { get; set; } // Income | Expense | Audit
        public Guid? ReferenceId { get; set; }
    }
}
