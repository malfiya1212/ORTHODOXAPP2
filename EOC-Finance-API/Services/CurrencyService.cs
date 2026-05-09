using EOC.Finance.API.Data;
using EOC.Finance.API.Models;
using Microsoft.EntityFrameworkCore;
using System.Threading.Tasks;

namespace EOC.Finance.API.Services
{
    public interface ICurrencyService
    {
        Task<decimal> ConvertToEtbAsync(decimal originalAmount, string currencyCode);
    }

    public class CurrencyService : ICurrencyService
    {
        private readonly AppDbContext _context;

        public CurrencyService(AppDbContext context)
        {
            _context = context;
        }

        public async Task<decimal> ConvertToEtbAsync(decimal originalAmount, string currencyCode)
        {
            if (currencyCode == "ETB") return originalAmount;

            var rate = await _context.Set<CurrencyRate>().FindAsync(currencyCode);
            if (rate == null) return originalAmount; // Fallback to 1:1 if rate unknown

            return originalAmount * rate.RateToEtb;
        }
    }
}
