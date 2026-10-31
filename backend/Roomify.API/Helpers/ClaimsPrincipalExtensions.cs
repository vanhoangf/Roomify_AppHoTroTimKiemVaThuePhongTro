using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using Roomify.API.Exceptions;

namespace Roomify.API.Helpers;

public static class ClaimsPrincipalExtensions
{
    public static long GetRequiredUserId(
        this ClaimsPrincipal user)
    {
        var userIdValue =
            user.FindFirst(
                JwtRegisteredClaimNames.Sub)?.Value;

        if (!long.TryParse(
            userIdValue,
            out var userId))
        {
            throw new UnauthorizedException(
                "INVALID_TOKEN_SUBJECT",
                "Token không chứa UserID hợp lệ.");
        }

        return userId;
    }
}