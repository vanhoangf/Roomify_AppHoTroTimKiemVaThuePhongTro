using System.Globalization;
using System.Net.Http.Json;

using Roomify.Admin.Models.Common;
using Roomify.Admin.Models.Statistics;

namespace Roomify.Admin.Services.Statistics;

public class AdminStatisticsApiService
    : IAdminStatisticsApiService
{
    private readonly IHttpClientFactory
        _httpClientFactory;

    private readonly ILogger<AdminStatisticsApiService>
        _logger;


    public AdminStatisticsApiService(
        IHttpClientFactory httpClientFactory,
        ILogger<AdminStatisticsApiService> logger)
    {
        _httpClientFactory =
            httpClientFactory;

        _logger =
            logger;
    }


    // ============================================================
    // GET STATISTICS
    // ============================================================

    public async Task<ApiResult<AdminStatisticsDto>>
        GetStatisticsAsync(
            DateOnly? fromDate = null,
            DateOnly? toDate = null,
            CancellationToken cancellationToken = default)
    {
        try
        {
            var query =
                new List<string>();


            if (fromDate.HasValue)
            {
                query.Add(
                    "fromDate="
                    +
                    fromDate.Value.ToString(
                        "yyyy-MM-dd",
                        CultureInfo.InvariantCulture));
            }


            if (toDate.HasValue)
            {
                query.Add(
                    "toDate="
                    +
                    toDate.Value.ToString(
                        "yyyy-MM-dd",
                        CultureInfo.InvariantCulture));
            }


            var url =
                "api/admin/statistics";


            if (query.Count > 0)
            {
                url +=
                    "?"
                    +
                    string.Join(
                        "&",
                        query);
            }


            var client =
                CreateClient();


            var response =
                await client.GetAsync(
                    url,
                    cancellationToken);


            if (!response.IsSuccessStatusCode)
            {
                return await ReadFailureAsync<AdminStatisticsDto>(
                    response,
                    "Không thể tải dữ liệu thống kê.",
                    cancellationToken);
            }


            var data =
                await response.Content
                    .ReadFromJsonAsync<AdminStatisticsDto>(
                        cancellationToken:
                            cancellationToken);


            if (data is null)
            {
                return ApiResult<AdminStatisticsDto>
                    .Failure(
                        "Roomify.API trả về dữ liệu thống kê không hợp lệ.");
            }


            return ApiResult<AdminStatisticsDto>
                .Success(
                    data);
        }
        catch (HttpRequestException ex)
        {
            _logger.LogWarning(
                ex,
                "Không thể kết nối Roomify.API khi tải thống kê.");


            return ApiResult<AdminStatisticsDto>
                .Failure(
                    "Không thể kết nối tới Roomify.API.");
        }
        catch (TaskCanceledException ex)
        {
            _logger.LogWarning(
                ex,
                "Roomify.API phản hồi quá thời gian khi tải thống kê.");


            return ApiResult<AdminStatisticsDto>
                .Failure(
                    "Roomify.API phản hồi quá thời gian.");
        }
        catch (Exception ex)
        {
            _logger.LogError(
                ex,
                "Lỗi khi tải dữ liệu thống kê Admin.");


            return ApiResult<AdminStatisticsDto>
                .Failure(
                    "Không thể tải dữ liệu thống kê.");
        }
    }


    // ============================================================
    // ERROR RESPONSE
    // ============================================================

    private static async Task<ApiResult<T>>
        ReadFailureAsync<T>(
            HttpResponseMessage response,
            string defaultMessage,
            CancellationToken cancellationToken)
    {
        ApiErrorResponseDto? error =
            null;


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
            // API trả lỗi không đúng JSON chuẩn.
        }


        return ApiResult<T>
            .Failure(
                error?.Message
                    ?? defaultMessage,
                error?.ErrorCode,
                (int)response.StatusCode);
    }


    // ============================================================
    // HTTP CLIENT
    // ============================================================

    private HttpClient CreateClient()
    {
        return _httpClientFactory
            .CreateClient(
                "RoomifyAdminApi");
    }
}