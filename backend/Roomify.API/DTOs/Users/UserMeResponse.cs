namespace Roomify.API.DTOs.Users;

public class UserMeResponse
{
    public long UserId { get; set; }

    public string Email { get; set; } = string.Empty;

    public string FullName { get; set; } = string.Empty;

    public string? Phone { get; set; }

    public string? AvatarUrl { get; set; }

    public string Role { get; set; } = string.Empty;

    public string Status { get; set; } = string.Empty;

    public bool EmailVerified { get; set; }

    public DateTime? LastLoginAt { get; set; }

    public DateTime CreatedAt { get; set; }

    public DateTime UpdatedAt { get; set; }

    public StudentProfileResponse? StudentProfile { get; set; }

    public LandlordProfileResponse? LandlordProfile { get; set; }
}