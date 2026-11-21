using System.Net.Http.Json;

using Roomify.Admin.Models.Common;
using Roomify.Admin.Models.Reviews;

namespace Roomify.Admin.Services.Reviews;

public class AdminReviewApiService
    : IAdminReviewApiService
{
    private readonly IHttpClientFactory
        _httpClientFactory;

    private readonly ILogger<AdminReviewApiService>
        _logger;


    public AdminReviewApiService(
        IHttpClientFactory httpClientFactory,
        ILogger<AdminReviewApiService> logger)
    {
        _httpClientFactory =
            httpClientFactory;

        _logger =
            logger;
    }


    // ============================================================
    // GET LIST
    // ============================================================

    public async Task<ApiResult<AdminReviewListDto>>
        GetReviewsAsync(
            string? status = null,
            int? rating = null,
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


            if (rating.HasValue)
            {
                query.Add(
                    $"rating={rating.Value}");
            }


            if (!string.IsNullOrWhiteSpace(search))
            {
                query.Add(
                    $"search={Uri.EscapeDataString(search.Trim())}");
            }


            var url =
                $"api/admin/reviews?{string.Join("&", query)}";


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
                        .ReadFromJsonAsync<AdminReviewListDto>(
                            cancellationToken:
                                cancellationToken);


                if (data is null)
                {
                    return ApiResult<AdminReviewListDto>
                        .Failure(
                            "Roomify.API trả về danh sách đánh giá không hợp lệ.");
                }


                return ApiResult<AdminReviewListDto>
                    .Success(data);
            }


            return await ReadFailureAsync<AdminReviewListDto>(
                response,
                "Không thể tải danh sách đánh giá.",
                cancellationToken);
        }
        catch (HttpRequestException ex)
        {
            _logger.LogWarning(
                ex,
                "Không thể kết nối Roomify.API khi tải danh sách đánh giá.");

            return ApiResult<AdminReviewListDto>.Failure(
                "Không thể kết nối tới Roomify.API.");
        }
        catch (TaskCanceledException ex)
        {
            _logger.LogWarning(
                ex,
                "Roomify.API phản hồi quá thời gian.");

            return ApiResult<AdminReviewListDto>.Failure(
                "Roomify.API phản hồi quá thời gian.");
        }
        catch (Exception ex)
        {
            _logger.LogError(
                ex,
                "Lỗi khi tải danh sách đánh giá.");

            return ApiResult<AdminReviewListDto>.Failure(
                "Đã xảy ra lỗi khi tải danh sách đánh giá.");
        }
    }


    // ============================================================
    // GET DETAIL
    // ============================================================

    public async Task<ApiResult<AdminReviewDto>>
        GetReviewByIdAsync(
            long reviewId,
            CancellationToken cancellationToken = default)
    {
        try
        {
            var client =
                _httpClientFactory.CreateClient(
                    "RoomifyAdminApi");


            var response =
                await client.GetAsync(
                    $"api/admin/reviews/{reviewId}",
                    cancellationToken);


            return await ReadReviewResponseAsync(
                response,
                "Không thể tải thông tin đánh giá.",
                cancellationToken);
        }
        catch (HttpRequestException ex)
        {
            _logger.LogWarning(
                ex,
                "Không thể kết nối API khi tải ReviewId {ReviewId}.",
                reviewId);

            return ApiResult<AdminReviewDto>.Failure(
                "Không thể kết nối tới Roomify.API.");
        }
        catch (Exception ex)
        {
            _logger.LogError(
                ex,
                "Lỗi khi tải ReviewId {ReviewId}.",
                reviewId);

            return ApiResult<AdminReviewDto>.Failure(
                "Đã xảy ra lỗi khi tải thông tin đánh giá.");
        }
    }


    // ============================================================
    // HIDE
    // ============================================================

    public async Task<ApiResult<AdminReviewDto>>
        HideReviewAsync(
            long reviewId,
            CancellationToken cancellationToken = default)
    {
        return await SendPatchWithoutBodyAsync(
            $"api/admin/reviews/{reviewId}/hide",
            "Không thể ẩn đánh giá.",
            cancellationToken);
    }


    // ============================================================
    // SHOW
    // ============================================================

    public async Task<ApiResult<AdminReviewDto>>
        ShowReviewAsync(
            long reviewId,
            CancellationToken cancellationToken = default)
    {
        return await SendPatchWithoutBodyAsync(
            $"api/admin/reviews/{reviewId}/show",
            "Không thể hiển thị lại đánh giá.",
            cancellationToken);
    }


    // ============================================================
    // PATCH WITHOUT BODY
    // ============================================================

    private async Task<ApiResult<AdminReviewDto>>
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


            return await ReadReviewResponseAsync(
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

            return ApiResult<AdminReviewDto>.Failure(
                "Không thể kết nối tới Roomify.API.");
        }
        catch (Exception ex)
        {
            _logger.LogError(
                ex,
                "Lỗi khi gọi PATCH {Url}.",
                url);

            return ApiResult<AdminReviewDto>.Failure(
                defaultMessage);
        }
    }


    // ============================================================
    // READ REVIEW RESPONSE
    // ============================================================

    private static async Task<ApiResult<AdminReviewDto>>
        ReadReviewResponseAsync(
            HttpResponseMessage response,
            string defaultMessage,
            CancellationToken cancellationToken)
    {
        if (response.IsSuccessStatusCode)
        {
            var data =
                await response.Content
                    .ReadFromJsonAsync<AdminReviewDto>(
                        cancellationToken:
                            cancellationToken);


            if (data is null)
            {
                return ApiResult<AdminReviewDto>
                    .Failure(
                        "Roomify.API trả về dữ liệu đánh giá không hợp lệ.");
            }


            return ApiResult<AdminReviewDto>
                .Success(data);
        }


        return await ReadFailureAsync<AdminReviewDto>(
            response,
            defaultMessage,
            cancellationToken);
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
            // Backend response lỗi không đúng JSON chuẩn.
        }


        return ApiResult<T>.Failure(
            error?.Message
                ?? defaultMessage,
            error?.ErrorCode,
            (int)response.StatusCode);
    }
}