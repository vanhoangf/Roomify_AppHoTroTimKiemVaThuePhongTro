using System.Net.Http.Json;

using Roomify.Admin.Models.Common;
using Roomify.Admin.Models.Contracts;

namespace Roomify.Admin.Services.Contracts;

public class AdminContractApiService
    : IAdminContractApiService
{
    private readonly IHttpClientFactory
        _httpClientFactory;

    private readonly ILogger<AdminContractApiService>
        _logger;


    public AdminContractApiService(
        IHttpClientFactory httpClientFactory,
        ILogger<AdminContractApiService> logger)
    {
        _httpClientFactory =
            httpClientFactory;

        _logger =
            logger;
    }


    // ============================================================
    // LIST
    // ============================================================

    public async Task<ApiResult<AdminContractListDto>>
        GetContractsAsync(
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


            if (!string.IsNullOrWhiteSpace(
                    status))
            {
                query.Add(
                    $"status={Uri.EscapeDataString(status)}");
            }


            if (!string.IsNullOrWhiteSpace(
                    search))
            {
                query.Add(
                    $"search={Uri.EscapeDataString(search)}");
            }


            var url =
                "api/admin/contracts?"
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
                return await ReadFailureAsync<AdminContractListDto>(
                    response,
                    "Không thể tải danh sách hợp đồng.",
                    cancellationToken);
            }


            var data =
                await response.Content
                    .ReadFromJsonAsync<AdminContractListDto>(
                        cancellationToken:
                            cancellationToken);


            if (data is null)
            {
                return ApiResult<AdminContractListDto>
                    .Failure(
                        "Roomify.API trả về dữ liệu không hợp lệ.");
            }


            return ApiResult<AdminContractListDto>
                .Success(data);
        }
        catch (HttpRequestException ex)
        {
            _logger.LogWarning(
                ex,
                "Không thể kết nối Roomify.API khi tải danh sách hợp đồng.");


            return ApiResult<AdminContractListDto>
                .Failure(
                    "Không thể kết nối tới Roomify.API.");
        }
        catch (TaskCanceledException ex)
        {
            _logger.LogWarning(
                ex,
                "Roomify.API phản hồi quá thời gian khi tải hợp đồng.");


            return ApiResult<AdminContractListDto>
                .Failure(
                    "Roomify.API phản hồi quá thời gian.");
        }
        catch (Exception ex)
        {
            _logger.LogError(
                ex,
                "Lỗi khi tải danh sách hợp đồng.");


            return ApiResult<AdminContractListDto>
                .Failure(
                    "Không thể tải danh sách hợp đồng.");
        }
    }


    // ============================================================
    // DETAIL
    // ============================================================

    public async Task<ApiResult<AdminContractDto>>
        GetContractByIdAsync(
            long contractId,
            CancellationToken cancellationToken = default)
    {
        try
        {
            var client =
                CreateClient();


            var response =
                await client.GetAsync(
                    $"api/admin/contracts/{contractId}",
                    cancellationToken);


            if (!response.IsSuccessStatusCode)
            {
                return await ReadFailureAsync<AdminContractDto>(
                    response,
                    "Không thể tải thông tin hợp đồng.",
                    cancellationToken);
            }


            var data =
                await response.Content
                    .ReadFromJsonAsync<AdminContractDto>(
                        cancellationToken:
                            cancellationToken);


            if (data is null)
            {
                return ApiResult<AdminContractDto>
                    .Failure(
                        "Roomify.API trả về dữ liệu không hợp lệ.");
            }


            return ApiResult<AdminContractDto>
                .Success(data);
        }
        catch (HttpRequestException ex)
        {
            _logger.LogWarning(
                ex,
                "Không thể kết nối Roomify.API khi tải hợp đồng {ContractId}.",
                contractId);


            return ApiResult<AdminContractDto>
                .Failure(
                    "Không thể kết nối tới Roomify.API.");
        }
        catch (TaskCanceledException ex)
        {
            _logger.LogWarning(
                ex,
                "Roomify.API phản hồi quá thời gian khi tải hợp đồng {ContractId}.",
                contractId);


            return ApiResult<AdminContractDto>
                .Failure(
                    "Roomify.API phản hồi quá thời gian.");
        }
        catch (Exception ex)
        {
            _logger.LogError(
                ex,
                "Lỗi khi tải hợp đồng {ContractId}.",
                contractId);


            return ApiResult<AdminContractDto>
                .Failure(
                    "Không thể tải thông tin hợp đồng.");
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