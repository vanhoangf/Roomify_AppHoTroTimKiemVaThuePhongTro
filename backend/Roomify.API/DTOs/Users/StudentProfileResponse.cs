namespace Roomify.API.DTOs.Users;

public class StudentProfileResponse
{
    public long StudentId { get; set; }

    public long? UniversityId { get; set; }

    public string? UniversityName { get; set; }

    public string? StudentCode { get; set; }

    public string? StudentCardUrl { get; set; }

    public bool IsVerified { get; set; }

    public DateTime? VerifiedAt { get; set; }
}