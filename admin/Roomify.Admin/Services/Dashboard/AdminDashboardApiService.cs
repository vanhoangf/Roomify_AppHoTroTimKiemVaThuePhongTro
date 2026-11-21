using System.Net.Http.Json;
using Roomify.Admin.Models.Common;
using Roomify.Admin.Models.Dashboard;

namespace Roomify.Admin.Services.Dashboard;

public class AdminDashboardApiService
    : IAdminDashboardApiService
{
    private readonly IHttpClientFactory _httpClientFactory;

    private readonly ILogger<AdminDashboardApiService> _logger;


    public AdminDashboardApiService(
        IHttpClientFactory httpClientFactory,
        ILogger<AdminDashboardApiService> logger)
    {
        _httpClientFactory = httpClientFactory;
        _logger = logger;
    }


    public async Task<ApiResult<AdminDashboardDto>>
        GetDashboardAsync(
            CancellationToken cancellationToken = default)
    {
        try
        {
            var client =
                _httpClientFactory.CreateClient(
                    "RoomifyAdminApi");

            var response =
                await client.GetAsync(
                    "api/admin/dashboard",
                    cancellationToken);


            if (response.IsSuccessStatusCode)
            {
                var data =
                    await response.Content
                        .ReadFromJsonAsync<AdminDashboardDto>(
                            cancellationToken:
                                cancellationToken);

                if (data is null)
                {
                    return ApiResult<AdminDashboardDto>
                        .Failure(
                            "Roomify.API trả về dữ liệu Dashboard không hợp lệ.");
                }

                return ApiResult<AdminDashboardDto>
                    .Success(data);
            }


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


            return ApiResult<AdminDashboardDto>.Failure(
                error?.Message
                    ?? "Không thể tải dữ liệu Dashboard.",
                error?.ErrorCode,
                (int)response.StatusCode);
        }
        catch (HttpRequestException ex)
        {
            _logger.LogWarning(
                ex,
                "Không thể kết nối Roomify.API khi tải Dashboard.");

            return ApiResult<AdminDashboardDto>.Failure(
                "Không thể kết nối tới Roomify.API.");
        }
        catch (TaskCanceledException ex)
        {
            _logger.LogWarning(
                ex,
                "Roomify.API phản hồi quá thời gian khi tải Dashboard.");

            return ApiResult<AdminDashboardDto>.Failure(
                "Roomify.API phản hồi quá thời gian.");
        }
        catch (Exception ex)
        {
            _logger.LogError(
                ex,
                "Lỗi khi tải Admin Dashboard.");

            return ApiResult<AdminDashboardDto>.Failure(
                "Đã xảy ra lỗi khi tải Dashboard.");
        }
    }
}