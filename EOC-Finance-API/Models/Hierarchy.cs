using System.ComponentModel.DataAnnotations;

namespace EOC.Finance.API.Models
{
    public class Diocese
    {
        [Key]
        public long Id { get; set; }

        [Required]
        public string Name { get; set; } = string.Empty;

        public string? BishopName { get; set; }
        
        public string? Description { get; set; }
    }

    public class Church
    {
        [Key]
        public long Id { get; set; }

        [Required]
        public string Name { get; set; } = string.Empty;

        public string? Location { get; set; }

        public double? Latitude { get; set; }
        public double? Longitude { get; set; }

        public long DioceseId { get; set; }
        
        public string Status { get; set; } = "ACTIVE";
    }
}
