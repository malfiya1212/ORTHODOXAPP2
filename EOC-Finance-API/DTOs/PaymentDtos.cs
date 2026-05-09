namespace EOC.Finance.API.DTOs
{
    public class PaymentCreateDto
    {
        public string PaymentType { get; set; } = string.Empty; 
        public decimal OriginalAmount { get; set; }
        public string CurrencyCode { get; set; } = "ETB";
        public long ChurchId { get; set; }
        public long? MemberId { get; set; }
        public string PaymentMethod { get; set; } = string.Empty; 
        public string? ReferenceNumber { get; set; }
        public string Status { get; set; } = "Completed";
    }

    public class PaymentReceiptDto
    {
        public Guid PaymentId { get; set; }
        public string ReceiptNumber { get; set; } = string.Empty;
        public string ChurchName { get; set; } = string.Empty;
        public string? MemberName { get; set; }
        public decimal Amount { get; set; }
        public string PaymentType { get; set; } = string.Empty;
        public string PaymentMethod { get; set; } = string.Empty;
        public string? ReferenceNumber { get; set; }
        public DateTime PaymentDate { get; set; }
        public string GeneratedBy { get; set; } = string.Empty;
        public string VerificationUrl { get; set; } = string.Empty;
    }
}
