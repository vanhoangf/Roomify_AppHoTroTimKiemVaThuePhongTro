using System.Net.Http.Json;
using Roomify.Admin.Models.Common;
using Roomify.Admin.Models.Users;

namespace Roomify.Admin.Services.Users;

public class AdminUserApiService
    : IAdminUserApiService
{
    private readonly IHttpClientFactory _httpClientFactory;
    private readonly ILogger<AdminUserApiService> _logger;

    public AdminUserApiService(
        IHttpClientFactory httpClientFactory,
        ILogger<AdminUserApiService> logger)
    {
        _httpClientFactory = httpClientFactory;
        _logger = logger;
    }


    public async Task<ApiResult<AdminUserListDto>> GetUsersAsync(
        string? role = null,
        string? status = null,
        bool? verified = null,
        string? search = null,
        int page = 1,
        int pageSize = 8,
        CancellationToken cancellationToken = default)
    {
        try
        {
            var query = new List<string>
            {
                $"page={page}",
                $"pageSize={pageSize}"
            };

            if (!string.IsNullOrWhiteSpace(role))
            {
                query.Add(
                    $"role={Uri.EscapeDataString(role)}");
            }

            if (!string.IsNullOrWhiteSpace(status))
            {
                query.Add(
                    $"status={Uri.EscapeDataString(status)}");
            }

            if (verified.HasValue)
            {
                query.Add(
                    $"verified={verified.Value.ToString().ToLowerInvariant()}");
            }

            if (!string.IsNullOrWhiteSpace(search))
            {
                query.Add(
                    $"search={Uri.EscapeDataString(search.Trim())}");
            }

            var url =
                $"api/admin/users?{string.Join("&", query)}";

            var client =
                _httpClientFactory.CreateClient(
                    "RoomifyAdminApi");

            var response =
                await client.GetAsync(
                    url,
                    cancellationToken);

            if (response.IsSuccessStatusCode)
            {
                var data =
                    await response.Content
                        .ReadFromJsonAsync<AdminUserListDto>(
                            cancellationToken:
                                cancellationToken);

                if (data is null)
                {
                    return ApiResult<AdminUserListDto>.Failure(
                        "Roomify.API trả về danh sách người dùng không hợp lệ.");
                }

                return ApiResult<AdminUserListDto>.Success(
                    data);
            }

            return await ReadFailureAsync<AdminUserListDto>(
                response,
                "Không thể tải danh sách người dùng.",
                cancellationToken);
        }
        catch (HttpRequestException ex)
        {
            _logger.LogWarning(
                ex,
                "Không thể kết nối Roomify.API khi tải người dùng.");

            return ApiResult<AdminUserListDto>.Failure(
                "Không thể kết nối tới Roomify.API.");
        }
        catch (TaskCanceledException ex)
        {
            _logger.LogWarning(
                ex,
                "Roomify.API phản hồi quá thời gian.");

            return ApiResult<AdminUserListDto>.Failure(
                "Roomify.API phản hồi quá thời gian.");
        }
        catch (Exception ex)
        {
            _logger.LogError(
                ex,
                "Lỗi khi tải danh sách người dùng.");

            return ApiResult<AdminUserListDto>.Failure(
                "Đã xảy ra lỗi khi tải danh sách người dùng.");
        }
    }


    public async Task<ApiResult<AdminUserDto>> GetUserByIdAsync(
        long userId,
        CancellationToken cancellationToken = default)
    {
        return await SendGetUserAsync(
            $"api/admin/users/{userId}",
            cancellationToken);
    }


    public async Task<ApiResult<AdminUserDto>> LockUserAsync(
        long userId,
        string reason,
        CancellationToken cancellationToken = default)
    {
        try
        {
            var client =
                _httpClientFactory.CreateClient(
                    "RoomifyAdminApi");

            var response =
                await client.PatchAsJsonAsync(
                    $"api/admin/users/{userId}/lock",
                    new LockUserRequestDto
                    {
                        Reason = reason.Trim()
                    },
                    cancellationToken);

            return await ReadUserResponseAsync(
                response,
                "Không thể khóa tài khoản.",
                cancellationToken);
        }
        catch (Exception ex)
        {
            _logger.LogError(
                ex,
                "Lỗi khi khóa UserId {UserId}.",
                userId);

            return ApiResult<AdminUserDto>.Failure(
                "Đã xảy ra lỗi khi khóa tài khoản.");
        }
    }


    public async Task<ApiResult<AdminUserDto>> UnlockUserAsync(
        long userId,
        CancellationToken cancellationToken = default)
    {
        return await SendPatchWithoutBodyAsync(
            $"api/admin/users/{userId}/unlock",
            "Không thể mở khóa tài khoản.",
            cancellationToken);
    }


    public async Task<ApiResult<AdminUserDto>> VerifyStudentAsync(
        long studentId,
        CancellationToken cancellationToken = default)
    {
        return await SendPatchWithoutBodyAsync(
            $"api/admin/students/{studentId}/verify",
            "Không thể xác minh sinh viên.",
            cancellationToken);
    }


    public async Task<ApiResult<AdminUserDto>> VerifyLandlordAsync(
        long landlordId,
        CancellationToken cancellationToken = default)
    {
        return await SendPatchWithoutBodyAsync(
            $"api/admin/landlords/{landlordId}/verify",
            "Không thể xác minh chủ trọ.",
            cancellationToken);
    }


    private async Task<ApiResult<AdminUserDto>> SendGetUserAsync(
        string url,
        CancellationToken cancellationToken)
    {
        try
        {
            var client =
                _httpClientFactory.CreateClient(
                    "RoomifyAdminApi");

            var response =
                await client.GetAsync(
                    url,
                    cancellationToken);

            return await ReadUserResponseAsync(
                response,
                "Không thể tải thông tin người dùng.",
                cancellationToken);
        }
        catch (Exception ex)
        {
            _logger.LogError(
                ex,
                "Lỗi khi gọi {Url}.",
                url);

            return ApiResult<AdminUserDto>.Failure(
                "Đã xảy ra lỗi khi tải thông tin người dùng.");
        }
    }


    private async Task<ApiResult<AdminUserDto>>
        SendPatchWithoutBodyAsync(
            string url,
            string defaultMessage,
            CancellationToken cancellationToken)
    {
        try
        {
            var client =
                _httpClientFactory.CreateClient(
                    "RoomifyAdminApi");

            using var request =
                new HttpRequestMessage(
                    HttpMethod.Patch,
                    url);

            var response =
                await client.SendAsync(
                    request,
                    cancellationToken);

            return await ReadUserResponseAsync(
                response,
                defaultMessage,
                cancellationToken);
        }
        catch (Exception ex)
        {
            _logger.LogError(
                ex,
                "Lỗi khi gọi PATCH {Url}.",
                url);

            return ApiResult<AdminUserDto>.Failure(
                defaultMessage);
        }
    }


    private static async Task<ApiResult<AdminUserDto>>
        ReadUserResponseAsync(
            HttpResponseMessage response,
            string defaultMessage,
            CancellationToken cancellationToken)
    {
        if (response.IsSuccessStatusCode)
        {
            var data =
                await response.Content
                    .ReadFromJsonAsync<AdminUserDto>(
                        cancellationToken:
                            cancellationToken);

            if (data is null)
            {
                return ApiResult<AdminUserDto>.Failure(
                    "Roomify.API trả về dữ liệu người dùng không hợp lệ.");
            }

            return ApiResult<AdminUserDto>.Success(
                data);
        }

        return await ReadFailureAsync<AdminUserDto>(
            response,
            defaultMessage,
            cancellationToken);
    }


    private static async Task<ApiResult<T>> ReadFailureAsync<T>(
        HttpResponseMessage response,
        string defaultMessage,
        CancellationToken cancellationToken)
    {
        ApiErrorResponseDto? error = null;

        try
        {
            error =
                await response.Content
                    .ReadFromJsonAsync<ApiErrorResponseDto>(
                        cancellationToken:
                            cancellationToken);
        }
        catch
        {
            // Response lỗi không đúng JSON chuẩn.
        }

        return ApiResult<T>.Failure(
            error?.Message ?? defaultMessage,
            error?.ErrorCode,
            (int)response.StatusCode);
    }
}