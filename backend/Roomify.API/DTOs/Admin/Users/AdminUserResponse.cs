namespace Roomify.API.DTOs.Admin.Users;

public class AdminUserResponse
{
    public long UserId { get; set; }

    public int RoleId { get; set; }

    public string RoleCode { get; set; } = string.Empty;

    public string RoleName { get; set; } = string.Empty;

    public string Email { get; set; } = string.Empty;

    public string FullName { get; set; } = string.Empty;

    public string? Phone { get; set; }

    public string? AvatarUrl { get; set; }

    public string Status { get; set; } = string.Empty;

    public bool EmailVerified { get; set; }

    public string? LockedReason { get; set; }

    public DateTime? LockedAt { get; set; }

    public long? LockedByUserId { get; set; }

    public string? LockedByUserName { get; set; }

    public DateTime? LastLoginAt { get; set; }

    public long? StudentId { get; set; }

    public long? LandlordId { get; set; }

    public bool? IsProfileVerified { get; set; }

    public long? VerifiedByUserId { get; set; }

    public string? VerifiedByUserName { get; set; }

    public DateTime? VerifiedAt { get; set; }

    public long? UniversityId { get; set; }

    public string? UniversityName { get; set; }

    public string? StudentCode { get; set; }

    public string? StudentCardUrl { get; set; }

    public string? IdentityNumber { get; set; }

    public string? IdentityDocumentUrl { get; set; }

    public DateTime CreatedAt { get; set; }

    public DateTime UpdatedAt { get; set; }
}