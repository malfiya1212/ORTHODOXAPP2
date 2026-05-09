namespace EOC.Finance.API.DTOs
{
    public class ReportDto
    {
        public long Id { get; set; }
        public string Title { get; set; } = string.Empty;
        public string Type { get; set; } = string.Empty; // ANNUAL, MONTHLY
        public string Format { get; set; } = "PDF";
        public DateTime GeneratedAt { get; set; }
        public string? FileUrl { get; set; }
        public long ChurchId { get; set; }
        public string? ChurchName { get; set; }
    }
}
