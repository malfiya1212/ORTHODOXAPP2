namespace EOC.Finance.API.DTOs
{
    public class UserDto
    {
        public long Id { get; set; }
        public string Name { get; set; } = string.Empty;
        public string Email { get; set; } = string.Empty;
        public string? Phone { get; set; }
        public bool IsActive { get; set; }
        public long RoleId { get; set; }
        public string RoleName { get; set; } = string.Empty;
        public long? ChurchId { get; set; }
        public string? ChurchName { get; set; }
        public long? DioceseId { get; set; }
        public string? DioceseName { get; set; }
    }
}
