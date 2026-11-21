using System.Net.Http.Json;

using Roomify.Admin.Models.Common;
using Roomify.Admin.Models.Reports;

namespace Roomify.Admin.Services.Reports;

public class AdminReportApiService
    : IAdminReportApiService
{
    private readonly IHttpClientFactory
        _httpClientFactory;

    private readonly ILogger<AdminReportApiService>
        _logger;


    public AdminReportApiService(
        IHttpClientFactory httpClientFactory,
        ILogger<AdminReportApiService> logger)
    {
        _httpClientFactory =
            httpClientFactory;

        _logger =
            logger;
    }


    // ============================================================
    // GET LIST
    // ============================================================

    public async Task<ApiResult<AdminReportListDto>>
        GetReportsAsync(
            string? status = null,
            string? search = null,
            int page = 1,
            int pageSize = 10,
            CancellationToken cancellationToken = default)
    {
        try
        {
            var query =
                new List<string>
                {
                    $"page={page}",
                    $"pageSize={pageSize}"
                };


            if (!string.IsNullOrWhiteSpace(status))
            {
                query.Add(
                    $"status={Uri.EscapeDataString(status.Trim())}");
            }


            if (!string.IsNullOrWhiteSpace(search))
            {
                query.Add(
                    $"search={Uri.EscapeDataString(search.Trim())}");
            }


            var url =
                $"api/admin/reports?{string.Join("&", query)}";


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
                        .ReadFromJsonAsync<AdminReportListDto>(
                            cancellationToken:
                                cancellationToken);


                if (data is null)
                {
                    return ApiResult<AdminReportListDto>
                        .Failure(
                            "Roomify.API trả về danh sách báo cáo không hợp lệ.");
                }


                return ApiResult<AdminReportListDto>
                    .Success(data);
            }


            return await ReadFailureAsync<AdminReportListDto>(
                response,
                "Không thể tải danh sách báo cáo.",
                cancellationToken);
        }
        catch (HttpRequestException ex)
        {
            _logger.LogWarning(
                ex,
                "Không thể kết nối Roomify.API khi tải danh sách báo cáo.");

            return ApiResult<AdminReportListDto>.Failure(
                "Không thể kết nối tới Roomify.API.");
        }
        catch (TaskCanceledException ex)
        {
            _logger.LogWarning(
                ex,
                "Roomify.API phản hồi quá thời gian.");

            return ApiResult<AdminReportListDto>.Failure(
                "Roomify.API phản hồi quá thời gian.");
        }
        catch (Exception ex)
        {
            _logger.LogError(
                ex,
                "Lỗi khi tải danh sách báo cáo.");

            return ApiResult<AdminReportListDto>.Failure(
                "Đã xảy ra lỗi khi tải danh sách báo cáo.");
        }
    }


    // ============================================================
    // GET DETAIL
    // ============================================================

    public async Task<ApiResult<AdminReportDto>>
        GetReportByIdAsync(
            long reportId,
            CancellationToken cancellationToken = default)
    {
        try
        {
            var client =
                _httpClientFactory.CreateClient(
                    "RoomifyAdminApi");


            var response =
                await client.GetAsync(
                    $"api/admin/reports/{reportId}",
                    cancellationToken);


            return await ReadReportResponseAsync(
                response,
                "Không thể tải thông tin báo cáo.",
                cancellationToken);
        }
        catch (HttpRequestException ex)
        {
            _logger.LogWarning(
                ex,
                "Không thể kết nối API khi tải ReportId {ReportId}.",
                reportId);

            return ApiResult<AdminReportDto>.Failure(
                "Không thể kết nối tới Roomify.API.");
        }
        catch (Exception ex)
        {
            _logger.LogError(
                ex,
                "Lỗi khi tải ReportId {ReportId}.",
                reportId);

            return ApiResult<AdminReportDto>.Failure(
                "Đã xảy ra lỗi khi tải thông tin báo cáo.");
        }
    }


    // ============================================================
    // RESOLVE
    // ============================================================

    public async Task<ApiResult<AdminReportDto>>
        ResolveReportAsync(
            long reportId,
            string adminNote,
            CancellationToken cancellationToken = default)
    {
        return await SendDecisionAsync(
            $"api/admin/reports/{reportId}/resolve",
            adminNote,
            "Không thể xử lý báo cáo.",
            cancellationToken);
    }


    // ============================================================
    // REJECT
    // ============================================================

    public async Task<ApiResult<AdminReportDto>>
        RejectReportAsync(
            long reportId,
            string adminNote,
            CancellationToken cancellationToken = default)
    {
        return await SendDecisionAsync(
            $"api/admin/reports/{reportId}/reject",
            adminNote,
            "Không thể từ chối báo cáo.",
            cancellationToken);
    }


    // ============================================================
    // SEND DECISION
    // ============================================================

    private async Task<ApiResult<AdminReportDto>>
        SendDecisionAsync(
            string url,
            string adminNote,
            string defaultMessage,
            CancellationToken cancellationToken)
    {
        try
        {
            var client =
                _httpClientFactory.CreateClient(
                    "RoomifyAdminApi");


            var response =
                await client.PatchAsJsonAsync(
                    url,
                    new AdminReportDecisionRequestDto
                    {
                        AdminNote =
                            adminNote.Trim()
                    },
                    cancellationToken);


            return await ReadReportResponseAsync(
                response,
                defaultMessage,
                cancellationToken);
        }
        catch (HttpRequestException ex)
        {
            _logger.LogWarning(
                ex,
                "Không thể kết nối API khi gọi PATCH {Url}.",
                url);

            return ApiResult<AdminReportDto>.Failure(
                "Không thể kết nối tới Roomify.API.");
        }
        catch (Exception ex)
        {
            _logger.LogError(
                ex,
                "Lỗi khi gọi PATCH {Url}.",
                url);

            return ApiResult<AdminReportDto>.Failure(
                defaultMessage);
        }
    }


    // ============================================================
    // READ REPORT RESPONSE
    // ============================================================

    private static async Task<ApiResult<AdminReportDto>>
        ReadReportResponseAsync(
            HttpResponseMessage response,
            string defaultMessage,
            CancellationToken cancellationToken)
    {
        if (response.IsSuccessStatusCode)
        {
            var data =
                await response.Content
                    .ReadFromJsonAsync<AdminReportDto>(
                        cancellationToken:
                            cancellationToken);


            if (data is null)
            {
                return ApiResult<AdminReportDto>
                    .Failure(
                        "Roomify.API trả về dữ liệu báo cáo không hợp lệ.");
            }


            return ApiResult<AdminReportDto>
                .Success(data);
        }


        return await ReadFailureAsync<AdminReportDto>(
            response,
            defaultMessage,
            cancellationToken);
    }


    // ============================================================
    // FAILURE
    // ============================================================

    private static async Task<ApiResult<T>>
        ReadFailureAsync<T>(
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
            // Backend response không đúng JSON chuẩn.
        }


        return ApiResult<T>.Failure(
            error?.Message
                ?? defaultMessage,
            error?.ErrorCode,
            (int)response.StatusCode);
    }
}