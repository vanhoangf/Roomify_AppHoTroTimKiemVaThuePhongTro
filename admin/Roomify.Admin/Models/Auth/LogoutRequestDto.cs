namespace Roomify.Admin.Models.Auth;

public class LogoutRequestDto
{
    public string RefreshToken { get; set; } = string.Empty;
}