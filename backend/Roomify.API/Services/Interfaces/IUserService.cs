using Roomify.API.DTOs.Users;

namespace Roomify.API.Services.Interfaces;

public interface IUserService
{
    Task<UserMeResponse> GetMeAsync(
        long userId,
        CancellationToken cancellationToken = default);

    Task<UserMeResponse> UpdateMeAsync(
        long userId,
        UpdateUserRequest request,
        CancellationToken cancellationToken = default);

    Task<StudentProfileResponse> UpdateStudentProfileAsync(
        long userId,
        UpdateStudentProfileRequest request,
        CancellationToken cancellationToken = default);

    Task<LandlordProfileResponse> UpdateLandlordProfileAsync(
        long userId,
        UpdateLandlordProfileRequest request,
        CancellationToken cancellationToken = default);
}