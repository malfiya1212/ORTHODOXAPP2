using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace EOC.Finance.API.Models
{
    public class Notification
    {
        [Key]
        public Guid Id { get; set; } = Guid.NewGuid();

        [Required]
        public long UserId { get; set; }
        [ForeignKey("UserId")]
        public User? User { get; set; }

        public long? ChurchId { get; set; } // Nullable if it's a global or diocese-level alert

        [Required]
        public string Title { get; set; } = string.Empty;

        [Required]
        public string Message { get; set; } = string.Empty;

        [Required]
        public string Type { get; set; } = string.Empty; // PAYMENT_RECEIVED | EXPENSE_APPROVAL | LOW_FUNDS | REMINDER

        public bool IsRead { get; set; } = false;

        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        // Channels targeted for this alert
        public bool SentViaInApp { get; set; } = true;
        public bool SentViaEmail { get; set; } = false;
        public bool SentViaSms { get; set; } = false;
    }
}
