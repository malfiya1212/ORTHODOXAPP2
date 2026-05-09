using System;
using System.ComponentModel.DataAnnotations;

namespace EOC.Finance.API.Models
{
    public class SystemLog
    {
        [Key]
        public long Id { get; set; }

        public DateTime Timestamp { get; set; } = DateTime.UtcNow;

        [Required]
        public string Level { get; set; } = "INFO"; // INFO | WARN | ERROR | PERFORMANCE

        public string? Category { get; set; } // API | DATABASE | SECURITY | AUTH

        public string? Message { get; set; }

        public string? Exception { get; set; } // Stack trace for errors

        public string? IpAddress { get; set; }

        public string? RequestPath { get; set; }

        public long ElapsedMilliseconds { get; set; } // For performance logs
    }
}
