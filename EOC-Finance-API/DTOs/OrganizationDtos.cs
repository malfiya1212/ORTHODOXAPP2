namespace EOC.Finance.API.DTOs
{
    public class DioceseDto
    {
        public long Id { get; set; }
        public string Name { get; set; } = string.Empty;
        public string? BishopName { get; set; }
        public string? Description { get; set; }
    }

    public class ChurchDto
    {
        public long Id { get; set; }
        public string Name { get; set; } = string.Empty;
        public string? Location { get; set; }
        public double? Latitude { get; set; }
        public double? Longitude { get; set; }
        public long DioceseId { get; set; }
        public string? DioceseName { get; set; }
        public string Status { get; set; } = "ACTIVE";
    }
}
