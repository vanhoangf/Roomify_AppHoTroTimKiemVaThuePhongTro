using System.ComponentModel.DataAnnotations;

namespace Roomify.API.DTOs.Users;

public class UpdateLandlordProfileRequest
{
    [StringLength(
        30,
        ErrorMessage = "Số CCCD/giấy tờ không được vượt quá 30 ký tự.")]
    public string? IdentityNumber { get; set; }
}