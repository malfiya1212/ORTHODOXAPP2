namespace EOC.Finance.API.DTOs
{
    public class ChurchFinancialReportDto
    {
        public long ChurchId { get; set; }
        public string ChurchName { get; set; } = string.Empty;
        public decimal TotalIncome { get; set; }
        public decimal TotalExpenses { get; set; }
        public decimal NetBalance { get; set; }
        public DateTime GeneratedAt { get; set; }
    }

    public class DioceseFinancialReportDto
    {
        public long DioceseId { get; set; }
        public string DioceseName { get; set; } = string.Empty;
        public decimal SumTotalIncome { get; set; }
        public decimal SumTotalExpenses { get; set; }
        public decimal TotalNetBalance { get; set; }
        public List<ChurchFinancialReportDto> ChurchComparisons { get; set; } = new();
    }

    public class NationalFinancialReportDto
    {
        public decimal AllEthiopiaTotalIncome { get; set; }
        public decimal AllEthiopiaTotalExpenses { get; set; }
        public decimal NationalReserve { get; set; }
        public List<DioceseFinancialReportDto> DioceseRankings { get; set; } = new();
    }
}
