using System.Net.Http.Json;

using Roomify.Admin.Models.Common;
using Roomify.Admin.Models.Payments;

namespace Roomify.Admin.Services.Payments;

public class AdminPaymentApiService
    : IAdminPaymentApiService
{
    private readonly IHttpClientFactory
        _httpClientFactory;

    private readonly ILogger<AdminPaymentApiService>
        _logger;


    public AdminPaymentApiService(
        IHttpClientFactory httpClientFactory,
        ILogger<AdminPaymentApiService> logger)
    {
        _httpClientFactory =
            httpClientFactory;

        _logger =
            logger;
    }


    // ============================================================
    // LIST
    // ============================================================

    public async Task<ApiResult<AdminPaymentListDto>>
        GetPaymentsAsync(
            string? status = null,
            string? paymentType = null,
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


            if (!string.IsNullOrWhiteSpace(
                    status))
            {
                query.Add(
                    $"status={Uri.EscapeDataString(status)}");
            }


            if (!string.IsNullOrWhiteSpace(
                    paymentType))
            {
                query.Add(
                    $"paymentType={Uri.EscapeDataString(paymentType)}");
            }


            if (!string.IsNullOrWhiteSpace(
                    search))
            {
                query.Add(
                    $"search={Uri.EscapeDataString(search)}");
            }


            var url =
                "api/admin/payments?"
                +
                string.Join(
                    "&",
                    query);


            var client =
                CreateClient();


            var response =
                await client.GetAsync(
                    url,
                    cancellationToken);


            if (!response.IsSuccessStatusCode)
            {
                return await ReadFailureAsync<AdminPaymentListDto>(
                    response,
                    "Không thể tải danh sách thanh toán.",
                    cancellationToken);
            }


            var data =
                await response.Content
                    .ReadFromJsonAsync<AdminPaymentListDto>(
                        cancellationToken:
                            cancellationToken);


            if (data is null)
            {
                return ApiResult<AdminPaymentListDto>
                    .Failure(
                        "Roomify.API trả về dữ liệu không hợp lệ.");
            }


            return ApiResult<AdminPaymentListDto>
                .Success(
                    data);
        }
        catch (HttpRequestException ex)
        {
            _logger.LogWarning(
                ex,
                "Không thể kết nối Roomify.API khi tải danh sách thanh toán.");


            return ApiResult<AdminPaymentListDto>
                .Failure(
                    "Không thể kết nối tới Roomify.API.");
        }
        catch (TaskCanceledException ex)
        {
            _logger.LogWarning(
                ex,
                "Roomify.API phản hồi quá thời gian khi tải danh sách thanh toán.");


            return ApiResult<AdminPaymentListDto>
                .Failure(
                    "Roomify.API phản hồi quá thời gian.");
        }
        catch (Exception ex)
        {
            _logger.LogError(
                ex,
                "Lỗi khi tải danh sách thanh toán.");


            return ApiResult<AdminPaymentListDto>
                .Failure(
                    "Không thể tải danh sách thanh toán.");
        }
    }


    // ============================================================
    // DETAIL
    // ============================================================

    public async Task<ApiResult<AdminPaymentDto>>
        GetPaymentByIdAsync(
            long paymentId,
            CancellationToken cancellationToken = default)
    {
        try
        {
            var client =
                CreateClient();


            var response =
                await client.GetAsync(
                    $"api/admin/payments/{paymentId}",
                    cancellationToken);


            if (!response.IsSuccessStatusCode)
            {
                return await ReadFailureAsync<AdminPaymentDto>(
                    response,
                    "Không thể tải thông tin thanh toán.",
                    cancellationToken);
            }


            var data =
                await response.Content
                    .ReadFromJsonAsync<AdminPaymentDto>(
                        cancellationToken:
                            cancellationToken);


            if (data is null)
            {
                return ApiResult<AdminPaymentDto>
                    .Failure(
                        "Roomify.API trả về dữ liệu không hợp lệ.");
            }


            return ApiResult<AdminPaymentDto>
                .Success(
                    data);
        }
        catch (HttpRequestException ex)
        {
            _logger.LogWarning(
                ex,
                "Không thể kết nối Roomify.API khi tải payment {PaymentId}.",
                paymentId);


            return ApiResult<AdminPaymentDto>
                .Failure(
                    "Không thể kết nối tới Roomify.API.");
        }
        catch (TaskCanceledException ex)
        {
            _logger.LogWarning(
                ex,
                "Roomify.API phản hồi quá thời gian khi tải payment {PaymentId}.",
                paymentId);


            return ApiResult<AdminPaymentDto>
                .Failure(
                    "Roomify.API phản hồi quá thời gian.");
        }
        catch (Exception ex)
        {
            _logger.LogError(
                ex,
                "Lỗi khi tải payment {PaymentId}.",
                paymentId);


            return ApiResult<AdminPaymentDto>
                .Failure(
                    "Không thể tải thông tin thanh toán.");
        }
    }


    // ============================================================
    // ERROR
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


        return ApiResult<T>.Failure(
            error?.Message
                ?? defaultMessage,
            error?.ErrorCode,
            (int)response.StatusCode);
    }


    // ============================================================
    // CLIENT
    // ============================================================

    private HttpClient CreateClient()
    {
        return _httpClientFactory
            .CreateClient(
                "RoomifyAdminApi");
    }
}