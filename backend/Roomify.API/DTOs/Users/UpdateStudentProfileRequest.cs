using System.ComponentModel.DataAnnotations;

namespace Roomify.API.DTOs.Users;

public class UpdateStudentProfileRequest
{
    public long? UniversityId { get; set; }

    [StringLength(
        50,
        ErrorMessage = "Mã sinh viên không được vượt quá 50 ký tự.")]
    public string? StudentCode { get; set; }
}