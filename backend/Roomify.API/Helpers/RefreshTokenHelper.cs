using System.Security.Cryptography;
using System.Text;
using Microsoft.AspNetCore.WebUtilities;

namespace Roomify.API.Helpers;

public static class RefreshTokenHelper
{
    public static string GenerateToken()
    {
        var randomBytes =
            RandomNumberGenerator.GetBytes(64);

        return WebEncoders.Base64UrlEncode(
            randomBytes);
    }

    public static string HashToken(
        string token)
    {
        var tokenBytes =
            Encoding.UTF8.GetBytes(token);

        var hashBytes =
            SHA256.HashData(tokenBytes);

        return Convert.ToHexString(hashBytes);
    }
}