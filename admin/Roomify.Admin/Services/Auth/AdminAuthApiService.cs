using System.Net.Http.Json;
using Roomify.Admin.Models.Auth;
using Roomify.Admin.Models.Common;

namespace Roomify.Admin.Services.Auth;

public class AdminAuthApiService : IAdminAuthApiService
{
    private readonly IHttpClientFactory _httpClientFactory;
    private readonly ILogger<AdminAuthApiService> _logger;

    public AdminAuthApiService(
        IHttpClientFactory httpClientFactory,
        ILogger<AdminAuthApiService> logger)
    {
        _httpClientFactory = httpClientFactory;
        _logger = logger;
    }

    public async Task<ApiResult<LoginResponseDto>> LoginAsync(
        LoginRequestDto request,
        CancellationToken cancellationToken = default)
    {
        try
        {
            var client =
                _httpClientFactory.CreateClient("RoomifyApi");

            var response = await client.PostAsJsonAsync(
                "api/auth/login",
                request,
                cancellationToken);

            if (response.IsSuccessStatusCode)
            {
                var loginResponse =
                    await response.Content
                        .ReadFromJsonAsync<LoginResponseDto>(
                            cancellationToken: cancellationToken);

                if (loginResponse is null)
                {
                    return ApiResult<LoginResponseDto>.Failure(
                        "Roomify.API trả về dữ liệu đăng nhập không hợp lệ.");
                }

                return ApiResult<LoginResponseDto>.Success(
                    loginResponse);
            }

            var apiError =
                await response.Content
                    .ReadFromJsonAsync<ApiErrorResponseDto>(
                        cancellationToken: cancellationToken);

            return ApiResult<LoginResponseDto>.Failure(
                apiError?.Message
                    ?? "Không thể đăng nhập vào hệ thống.",
                apiError?.ErrorCode,
                (int)response.StatusCode);
        }
        catch (HttpRequestException ex)
        {
            _logger.LogWarning(
                ex,
                "Không thể kết nối tới Roomify.API.");

            return ApiResult<LoginResponseDto>.Failure(
                "Không thể kết nối tới Roomify.API. Vui lòng kiểm tra backend.");
        }
        catch (TaskCanceledException ex)
        {
            _logger.LogWarning(
                ex,
                "Roomify.API phản hồi quá thời gian.");

            return ApiResult<LoginResponseDto>.Failure(
                "Roomify.API phản hồi quá thời gian. Vui lòng thử lại.");
        }
        catch (Exception ex)
        {
            _logger.LogError(
                ex,
                "Lỗi không xác định khi đăng nhập Admin.");

            return ApiResult<LoginResponseDto>.Failure(
                "Đã xảy ra lỗi trong quá trình đăng nhập.");
        }
    }

    public async Task<ApiResult<bool>> LogoutAsync(
    string refreshToken,
    CancellationToken cancellationToken = default)
    {
        try
        {
            var client =
                _httpClientFactory.CreateClient("RoomifyApi");

            var response = await client.PostAsJsonAsync(
                "api/auth/logout",
                new LogoutRequestDto
                {
                    RefreshToken = refreshToken
                },
                cancellationToken);

            if (response.IsSuccessStatusCode)
            {
                return ApiResult<bool>.Success(true);
            }

            return ApiResult<bool>.Failure(
                "Không thể thu hồi phiên đăng nhập trên Roomify.API.",
                statusCode: (int)response.StatusCode);
        }
        catch (Exception ex)
        {
            _logger.LogWarning(
                ex,
                "Không thể gọi API logout.");

            return ApiResult<bool>.Failure(
                "Không thể kết nối Roomify.API khi đăng xuất.");
        }
    }
}