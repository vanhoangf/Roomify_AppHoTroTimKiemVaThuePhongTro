using System.Net.Http.Json;
using Roomify.Admin.Models.Common;
using Roomify.Admin.Models.Rooms;

namespace Roomify.Admin.Services.Rooms;

public class AdminRoomApiService
    : IAdminRoomApiService
{
    private readonly IHttpClientFactory
        _httpClientFactory;

    private readonly ILogger<AdminRoomApiService>
        _logger;


    public AdminRoomApiService(
        IHttpClientFactory httpClientFactory,
        ILogger<AdminRoomApiService> logger)
    {
        _httpClientFactory =
            httpClientFactory;

        _logger =
            logger;
    }


    // ============================================================
    // GET LIST
    // ============================================================

    public async Task<ApiResult<AdminRoomListDto>>
        GetRoomsAsync(
            string? listingStatus = null,
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
                listingStatus))
            {
                query.Add(
                    $"listingStatus={Uri.EscapeDataString(listingStatus)}");
            }


            if (!string.IsNullOrWhiteSpace(
                search))
            {
                query.Add(
                    $"search={Uri.EscapeDataString(search.Trim())}");
            }


            var url =
                $"api/admin/rooms?{string.Join("&", query)}";


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
                        .ReadFromJsonAsync<AdminRoomListDto>(
                            cancellationToken:
                                cancellationToken);


                if (data is null)
                {
                    return ApiResult<AdminRoomListDto>
                        .Failure(
                            "Roomify.API trả về danh sách phòng không hợp lệ.");
                }


                return ApiResult<AdminRoomListDto>
                    .Success(data);
            }


            return await ReadFailureAsync<AdminRoomListDto>(
                response,
                "Không thể tải danh sách phòng.",
                cancellationToken);
        }
        catch (HttpRequestException ex)
        {
            _logger.LogWarning(
                ex,
                "Không thể kết nối Roomify.API khi tải danh sách phòng.");

            return ApiResult<AdminRoomListDto>.Failure(
                "Không thể kết nối tới Roomify.API.");
        }
        catch (TaskCanceledException ex)
        {
            _logger.LogWarning(
                ex,
                "Roomify.API phản hồi quá thời gian.");

            return ApiResult<AdminRoomListDto>.Failure(
                "Roomify.API phản hồi quá thời gian.");
        }
        catch (Exception ex)
        {
            _logger.LogError(
                ex,
                "Lỗi khi tải danh sách phòng.");

            return ApiResult<AdminRoomListDto>.Failure(
                "Đã xảy ra lỗi khi tải danh sách phòng.");
        }
    }


    // ============================================================
    // GET DETAIL
    // ============================================================

    public async Task<ApiResult<AdminRoomDto>>
        GetRoomByIdAsync(
            long roomId,
            CancellationToken cancellationToken = default)
    {
        try
        {
            var client =
                _httpClientFactory.CreateClient(
                    "RoomifyAdminApi");


            var response =
                await client.GetAsync(
                    $"api/admin/rooms/{roomId}",
                    cancellationToken);


            return await ReadRoomResponseAsync(
                response,
                "Không thể tải thông tin phòng.",
                cancellationToken);
        }
        catch (HttpRequestException ex)
        {
            _logger.LogWarning(
                ex,
                "Không thể kết nối API khi tải RoomId {RoomId}.",
                roomId);

            return ApiResult<AdminRoomDto>.Failure(
                "Không thể kết nối tới Roomify.API.");
        }
        catch (Exception ex)
        {
            _logger.LogError(
                ex,
                "Lỗi khi tải RoomId {RoomId}.",
                roomId);

            return ApiResult<AdminRoomDto>.Failure(
                "Đã xảy ra lỗi khi tải thông tin phòng.");
        }
    }


    // ============================================================
    // APPROVE
    // ============================================================

    public async Task<ApiResult<AdminRoomDto>>
        ApproveRoomAsync(
            long roomId,
            CancellationToken cancellationToken = default)
    {
        return await SendPatchWithoutBodyAsync(
            $"api/admin/rooms/{roomId}/approve",
            "Không thể duyệt tin phòng.",
            cancellationToken);
    }


    // ============================================================
    // REJECT
    // ============================================================

    public async Task<ApiResult<AdminRoomDto>>
        RejectRoomAsync(
            long roomId,
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
                    $"api/admin/rooms/{roomId}/reject",
                    new RejectRoomRequestDto
                    {
                        Reason =
                            reason.Trim()
                    },
                    cancellationToken);


            return await ReadRoomResponseAsync(
                response,
                "Không thể từ chối tin phòng.",
                cancellationToken);
        }
        catch (HttpRequestException ex)
        {
            _logger.LogWarning(
                ex,
                "Không thể kết nối API khi reject RoomId {RoomId}.",
                roomId);

            return ApiResult<AdminRoomDto>.Failure(
                "Không thể kết nối tới Roomify.API.");
        }
        catch (Exception ex)
        {
            _logger.LogError(
                ex,
                "Lỗi khi reject RoomId {RoomId}.",
                roomId);

            return ApiResult<AdminRoomDto>.Failure(
                "Đã xảy ra lỗi khi từ chối tin phòng.");
        }
    }


    // ============================================================
    // PATCH WITHOUT BODY
    // ============================================================

    private async Task<ApiResult<AdminRoomDto>>
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


            return await ReadRoomResponseAsync(
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

            return ApiResult<AdminRoomDto>.Failure(
                "Không thể kết nối tới Roomify.API.");
        }
        catch (Exception ex)
        {
            _logger.LogError(
                ex,
                "Lỗi khi gọi PATCH {Url}.",
                url);

            return ApiResult<AdminRoomDto>.Failure(
                defaultMessage);
        }
    }


    // ============================================================
    // READ ROOM RESPONSE
    // ============================================================

    private static async Task<ApiResult<AdminRoomDto>>
        ReadRoomResponseAsync(
            HttpResponseMessage response,
            string defaultMessage,
            CancellationToken cancellationToken)
    {
        if (response.IsSuccessStatusCode)
        {
            var data =
                await response.Content
                    .ReadFromJsonAsync<AdminRoomDto>(
                        cancellationToken:
                            cancellationToken);


            if (data is null)
            {
                return ApiResult<AdminRoomDto>
                    .Failure(
                        "Roomify.API trả về dữ liệu phòng không hợp lệ.");
            }


            return ApiResult<AdminRoomDto>
                .Success(data);
        }


        return await ReadFailureAsync<AdminRoomDto>(
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
            // Backend error body không đúng JSON chuẩn.
        }


        return ApiResult<T>.Failure(
            error?.Message
                ?? defaultMessage,
            error?.ErrorCode,
            (int)response.StatusCode);
    }
}