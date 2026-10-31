using System.ComponentModel.DataAnnotations;

namespace Roomify.API.DTOs.Auth;

public class RegisterStudentRequest
{
    [Required(ErrorMessage = "Họ tên là bắt buộc.")]
    [MaxLength(150, ErrorMessage = "Họ tên không được vượt quá 150 ký tự.")]
    public string FullName { get; set; } = string.Empty;

    [Required(ErrorMessage = "Email là bắt buộc.")]
    [EmailAddress(ErrorMessage = "Email không đúng định dạng.")]
    [MaxLength(255)]
    public string Email { get; set; } = string.Empty;

    [Required(ErrorMessage = "Mật khẩu là bắt buộc.")]
    [MinLength(8, ErrorMessage = "Mật khẩu phải có ít nhất 8 ký tự.")]
    [MaxLength(100)]
    public string Password { get; set; } = string.Empty;

    [MaxLength(20)]
    public string? Phone { get; set; }

    public long? UniversityId { get; set; }

    [MaxLength(50)]
    public string? StudentCode { get; set; }
}