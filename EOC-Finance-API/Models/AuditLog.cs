using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace EOC.Finance.API.Models
{
    public class AuditLog
    {
        [Key]
        public long Id { get; set; }

        public long? UserId { get; set; }

        [ForeignKey("UserId")]
        public User? User { get; set; }

        [Required]
        public string Action { get; set; } = string.Empty;

        public string? TableName { get; set; }

        public long? RecordId { get; set; }

        [Required]
        public string Details { get; set; } = string.Empty;

        [Required]
        public DateTime Timestamp { get; set; } = DateTime.UtcNow;
    }
}
