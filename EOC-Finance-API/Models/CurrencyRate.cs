using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace EOC.Finance.API.Models
{
    public class CurrencyRate
    {
        [Key]
        public string CurrencyCode { get; set; } = string.Empty; // USD, EUR, GBP, ETB
        
        [Column(TypeName = "decimal(18,4)")]
        public decimal RateToEtb { get; set; } // 1 Unit of Currency = X ETB

        public DateTime LastUpdated { get; set; } = DateTime.UtcNow;
    }
}
