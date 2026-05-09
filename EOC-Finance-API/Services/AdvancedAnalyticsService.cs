using EOC.Finance.API.Data;
using EOC.Finance.API.DTOs;
using Microsoft.EntityFrameworkCore;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace EOC.Finance.API.Services
{
    public interface IAdvancedAnalyticsService
    {
        Task<AdvancedAnalyticsReportDto> GetNationalFinancialReportAsync(int year);
    }

    public class AdvancedAnalyticsService : IAdvancedAnalyticsService
    {
        private readonly AppDbContext _context;

        public AdvancedAnalyticsService(AppDbContext context)
        {
            _context = context;
        }

        public async Task<AdvancedAnalyticsReportDto> GetNationalFinancialReportAsync(int year)
        {
            var report = new AdvancedAnalyticsReportDto();

            // 1. Income & Expense Trend Analysis (Monthly)
            for (int month = 1; month <= 12; month++)
            {
                var monthStart = new DateTime(year, month, 1);
                var monthEnd = monthStart.AddMonths(1).AddDays(-1);

                var income = await _context.IncomeRecords
                    .Where(i => i.Date >= monthStart && i.Date <= monthEnd && i.Status == "APPROVED")
                    .SumAsync(i => (decimal)i.Amount);

                var expense = await _context.Expenses
                    .Where(e => e.Date >= monthStart && e.Date <= monthEnd && e.Status == "FINAL_APPROVED")
                    .SumAsync(e => (decimal)e.Amount);

                report.YearlyTrends.Add(new MonthlyTrendDto
                {
                    Month = monthStart.ToString("MMM"),
                    TotalIncome = income,
                    TotalExpense = expense
                });
            }

            // 2. Church Performance Ranking
            var churches = await _context.Churches.ToListAsync();
            var performanceList = new List<ChurchPerformanceDto>();

            foreach (var church in churches)
            {
                var totalInc = await _context.IncomeRecords
                    .Where(i => i.ChurchId == church.Id && i.Status == "APPROVED")
                    .SumAsync(i => (decimal)i.Amount);

                var totalExp = await _context.Expenses
                    .Where(e => e.ChurchId == church.Id && e.Status == "FINAL_APPROVED")
                    .SumAsync(e => (decimal)e.Amount);

                performanceList.Add(new ChurchPerformanceDto
                {
                    ChurchId = church.Id,
                    ChurchName = church.Name,
                    NetBalance = totalInc - totalExp
                });
            }

            report.TopPerformingChurches = performanceList
                .OrderByDescending(p => p.NetBalance)
                .Select((p, index) => { p.Rank = index + 1; return p; })
                .Take(10)
                .ToList();

            // 3. Donation Behavior Tracking
            var donors = await _context.Donors.ToListAsync();
            if (donors.Any())
            {
                report.DonorInsights.TotalDonors = donors.Count;
                report.DonorInsights.AverageDonation = donors.Average(d => d.TotalDonatedAmount);
                
                // Simplified Retention calculation (Repeat vs Total)
                var repeatDonors = donors.Count(d => d.TotalDonatedAmount > (d.TotalDonatedAmount / 2)); // Mock logic
                report.DonorInsights.RetentionRate = (decimal)repeatDonors / donors.Count * 100;
            }

            return report;
        }
    }
}
