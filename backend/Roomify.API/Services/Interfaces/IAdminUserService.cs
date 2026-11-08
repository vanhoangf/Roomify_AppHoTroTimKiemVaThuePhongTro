using Roomify.API.DTOs.Admin.Users;

namespace Roomify.API.Services.Interfaces;

public interface IAdminUserService
{
    Task<AdminUserListResponse> GetUsersAsync(
        string? role,
        string? status,
        bool? verified,
        string? search,
        int page,
        int pageSize);

    Task<AdminUserResponse> GetUserByIdAsync(
        long userId);

    Task<AdminUserResponse> LockUserAsync(
        long adminUserId,
        long userId,
        LockUserRequest request);

    Task<AdminUserResponse> UnlockUserAsync(
        long adminUserId,
        long userId);

    Task<AdminUserResponse> VerifyStudentAsync(
        long adminUserId,
        long studentId);

    Task<AdminUserResponse> VerifyLandlordAsync(
        long adminUserId,
        long landlordId);
}