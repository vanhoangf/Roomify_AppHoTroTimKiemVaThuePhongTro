using Roomify.Admin.Models.Common;
using Roomify.Admin.Models.Users;

namespace Roomify.Admin.Services.Users;

public interface IAdminUserApiService
{
    Task<ApiResult<AdminUserListDto>> GetUsersAsync(
        string? role = null,
        string? status = null,
        bool? verified = null,
        string? search = null,
        int page = 1,
        int pageSize = 8,
        CancellationToken cancellationToken = default);

    Task<ApiResult<AdminUserDto>> GetUserByIdAsync(
        long userId,
        CancellationToken cancellationToken = default);

    Task<ApiResult<AdminUserDto>> LockUserAsync(
        long userId,
        string reason,
        CancellationToken cancellationToken = default);

    Task<ApiResult<AdminUserDto>> UnlockUserAsync(
        long userId,
        CancellationToken cancellationToken = default);

    Task<ApiResult<AdminUserDto>> VerifyStudentAsync(
        long studentId,
        CancellationToken cancellationToken = default);

    Task<ApiResult<AdminUserDto>> VerifyLandlordAsync(
        long landlordId,
        CancellationToken cancellationToken = default);
}