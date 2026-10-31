using Roomify.API.Entities;

namespace Roomify.API.Services.Interfaces;

public interface IJwtTokenService
{
    (string Token, DateTime ExpiresAt) GenerateAccessToken(
        User user,
        string roleCode);
}