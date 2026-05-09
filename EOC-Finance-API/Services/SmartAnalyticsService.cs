using EOC.Finance.API.Data;
using Microsoft.EntityFrameworkCore;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace EOC.Finance.API.Services
{
    public class AnomalyReport
    {
        public string EntityName { get; set; } = string.Empty;
        public decimal Amount { get; set; }
        public string Severity { get; set; } = "Low";
        public string Reason { get; set; } = string.Empty;
    }

    public interface ISmartAnalyticsService
    {
        Task<decimal> PredictNextMonthIncomeAsync(long churchId);
        Task<List<AnomalyReport>> DetectFinancialAnomaliesAsync(long churchId);
    }

    public class SmartAnalyticsService : ISmartAnalyticsService
    {
        private readonly AppDbContext _context;

        public SmartAnalyticsService(AppDbContext context)
        {
            _context = context;
        }

        public async Task<decimal> PredictNextMonthIncomeAsync(long churchId)
        {
            // Simple Linear Regression Simulation based on last 6 months of data
            var sixMonthsAgo = DateTime.UtcNow.AddMonths(-6);
            var historicalData = await _context.IncomeRecords
                .Where(i => i.ChurchId == churchId && i.Date >= sixMonthsAgo && i.Status == "APPROVED")
                .GroupBy(i => new { i.Date.Year, i.Date.Month })
                .Select(g => (decimal)g.Sum(x => x.Amount))
                .ToListAsync();

            if (!historicalData.Any()) return 0;

            // Calculate simple moving average and apply a 2% growth factor
            var average = historicalData.Average();
            return average * 1.02m; 
        }

        public async Task<List<AnomalyReport>> DetectFinancialAnomaliesAsync(long churchId)
        {
            var anomalies = new List<AnomalyReport>();
            var last30Days = DateTime.UtcNow.AddDays(-30);

            // Fetch recent expenses
            var recentExpenses = await _context.Expenses
                .Where(e => e.ChurchId == churchId && e.Date >= last30Days)
                .ToListAsync();

            // Fetch historical average for comparison
            var historicalAverageExpense = await _context.Expenses
                .Where(e => e.ChurchId == churchId && e.Date < last30Days)
                .Select(e => (decimal)e.Amount)
                .DefaultIfEmpty(0)
                .AverageAsync();

            foreach (var expense in recentExpenses)
            {
                // Rule: If an expense is > 300% of the historical average, flag as high-severity anomaly
                if (historicalAverageExpense > 0 && (decimal)expense.Amount > (historicalAverageExpense * 3))
                {
                    anomalies.Add(new AnomalyReport
                    {
                        EntityName = expense.Category,
                        Amount = (decimal)expense.Amount,
                        Severity = "CRITICAL",
                        Reason = "Transaction is 300% higher than historical parish average."
                    });
                }
            }

            return anomalies;
        }
    }
}
