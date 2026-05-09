using System.Collections.Generic;

namespace EOC.Finance.API.DTOs
{
    public class MonthlyTrendDto
    {
        public string Month { get; set; } = string.Empty;
        public decimal TotalIncome { get; set; }
        public decimal TotalExpense { get; set; }
    }

    public class ChurchPerformanceDto
    {
        public long ChurchId { get; set; }
        public string ChurchName { get; set; } = string.Empty;
        public decimal NetBalance { get; set; }
        public int Rank { get; set; }
    }

    public class DonorBehaviorDto
    {
        public int TotalDonors { get; set; }
        public decimal AverageDonation { get; set; }
        public decimal RetentionRate { get; set; } // Percentage of repeat donors
    }

    public class AdvancedAnalyticsReportDto
    {
        public List<MonthlyTrendDto> YearlyTrends { get; set; } = new();
        public List<ChurchPerformanceDto> TopPerformingChurches { get; set; } = new();
        public DonorBehaviorDto DonorInsights { get; set; } = new();
    }
}
