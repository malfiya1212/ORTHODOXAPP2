namespace EOC.Finance.API.DTOs
{
    public class IncomeDto
    {
        public long Id { get; set; }
        public double Amount { get; set; }
        public string Source { get; set; } = string.Empty;
        public string Category { get; set; } = "Tithe"; // አስራት
        public long Date { get; set; }
        public string Status { get; set; } = "PENDING";
        public long ChurchId { get; set; }
        public string? ChurchName { get; set; }
    }

    public class ExpenseDto
    {
        public long Id { get; set; }
        public double Amount { get; set; }
        public string Recipient { get; set; } = string.Empty;
        public string Category { get; set; } = string.Empty;
        public long Date { get; set; }
        public string Status { get; set; } = "PENDING";
        public long ChurchId { get; set; }
        public string? ChurchName { get; set; }
    }
}
