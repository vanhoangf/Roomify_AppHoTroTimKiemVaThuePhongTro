using Roomify.API.DTOs.Auth;

namespace Roomify.API.Services.Interfaces;

public interface IAuthService
{
    Task<AuthUserResponse> RegisterStudentAsync(
        RegisterStudentRequest request,
        CancellationToken cancellationToken = default);

    Task<AuthUserResponse> RegisterLandlordAsync(
        RegisterLandlordRequest request,
        CancellationToken cancellationToken = default);

    Task<LoginResponse> LoginAsync(
    LoginRequest request,
    CancellationToken cancellationToken = default);
}