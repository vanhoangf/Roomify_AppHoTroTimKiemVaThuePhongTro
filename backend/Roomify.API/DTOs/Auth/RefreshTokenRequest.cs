using System.ComponentModel.DataAnnotations;

namespace Roomify.API.DTOs.Auth;

public class RefreshTokenRequest
{
    [Required(ErrorMessage = "Refresh token là bắt buộc.")]
    public string RefreshToken { get; set; } = string.Empty;
}