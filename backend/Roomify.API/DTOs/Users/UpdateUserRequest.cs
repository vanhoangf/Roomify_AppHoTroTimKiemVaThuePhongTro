using System.ComponentModel.DataAnnotations;

namespace Roomify.API.DTOs.Users;

public class UpdateUserRequest
{
    [Required(ErrorMessage = "Họ tên là bắt buộc.")]
    [StringLength(
        150,
        ErrorMessage = "Họ tên không được vượt quá 150 ký tự.")]
    public string FullName { get; set; } = string.Empty;

    [StringLength(
        20,
        ErrorMessage = "Số điện thoại không được vượt quá 20 ký tự.")]
    public string? Phone { get; set; }
}