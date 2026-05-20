using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace EOC.Finance.API.Models
{
    public class User
    {
        [Key]
        public long Id { get; set; }
        
        [Required]
        public string Name { get; set; } = string.Empty;
        
        [Required]
        public string Email { get; set; } = string.Empty;
        
        [Required]
        public string PasswordHash { get; set; } = string.Empty;
        
        public string? Phone { get; set; }
        public bool IsActive { get; set; } = true;
        
        public long RoleId { get; set; }
        
        public long? ChurchId { get; set; }
        [ForeignKey("ChurchId")]
        public Church? Church { get; set; }
        
        public long? DioceseId { get; set; }
        [ForeignKey("DioceseId")]
        public Diocese? Diocese { get; set; }
    }
}
