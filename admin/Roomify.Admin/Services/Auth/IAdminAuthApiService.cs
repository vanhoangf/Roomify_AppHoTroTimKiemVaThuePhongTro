using Roomify.Admin.Models.Auth;
using Roomify.Admin.Models.Common;

namespace Roomify.Admin.Services.Auth;

public interface IAdminAuthApiService
{
    Task<ApiResult<LoginResponseDto>> LoginAsync(
        LoginRequestDto request,
        CancellationToken cancellationToken = default);
    Task<ApiResult<bool>> LogoutAsync(
        string refreshToken,
        CancellationToken cancellationToken = default);
}