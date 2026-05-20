using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace EOC.Finance.API.Models
{
    public class LoginHistory
    {
        [Key]
        public long Id { get; set; }

        [Required]
        public long UserId { get; set; }

        [ForeignKey("UserId")]
        public User? User { get; set; }

        [Required]
        public string IpAddress { get; set; } = string.Empty;

        [Required]
        public DateTime LoginTime { get; set; } = DateTime.UtcNow;
    }
}
