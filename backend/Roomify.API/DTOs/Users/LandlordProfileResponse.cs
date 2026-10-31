namespace Roomify.API.DTOs.Users;

public class LandlordProfileResponse
{
    public long LandlordId { get; set; }

    public string? IdentityNumber { get; set; }

    public string? IdentityDocumentUrl { get; set; }

    public bool IsVerified { get; set; }

    public DateTime? VerifiedAt { get; set; }
}