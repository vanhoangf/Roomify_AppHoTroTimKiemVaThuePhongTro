using System.ComponentModel.DataAnnotations;

namespace Roomify.Admin.Models.Auth;

public class LoginRequestDto
{
    [Required(ErrorMessage = "Vui lòng nhập email quản trị viên.")]
    [EmailAddress(ErrorMessage = "Email không đúng định dạng.")]
    public string Email { get; set; } = string.Empty;

    [Required(ErrorMessage = "Vui lòng nhập mật khẩu.")]
    public string Password { get; set; } = string.Empty;
}