using System.ComponentModel.DataAnnotations;

namespace Roomify.Admin.Models.Users;

public class LockUserRequestDto
{
    [Required(ErrorMessage = "Vui lòng nhập lý do khóa tài khoản.")]
    [MaxLength(
        500,
        ErrorMessage = "Lý do khóa không được vượt quá 500 ký tự.")]
    public string Reason { get; set; } = string.Empty;
}