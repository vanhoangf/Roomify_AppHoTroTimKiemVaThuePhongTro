using System.Net.Http.Json;

using Roomify.Admin.Models.Catalog;
using Roomify.Admin.Models.Common;

namespace Roomify.Admin.Services.Catalog;

public class AdminCatalogApiService
    : IAdminCatalogApiService
{
    private readonly IHttpClientFactory
        _httpClientFactory;

    private readonly ILogger<AdminCatalogApiService>
        _logger;


    public AdminCatalogApiService(
        IHttpClientFactory httpClientFactory,
        ILogger<AdminCatalogApiService> logger)
    {
        _httpClientFactory =
            httpClientFactory;

        _logger =
            logger;
    }


    // ============================================================
    // PROPERTY TYPES
    // ============================================================

    public Task<ApiResult<List<AdminPropertyTypeDto>>>
        GetPropertyTypesAsync(
            CancellationToken cancellationToken = default)
    {
        return GetListAsync<AdminPropertyTypeDto>(
            "api/admin/property-types",
            "Không thể tải danh sách loại hình nhà trọ.",
            cancellationToken);
    }


    public Task<ApiResult<AdminPropertyTypeDto>>
        CreatePropertyTypeAsync(
            SavePropertyTypeRequestDto request,
            CancellationToken cancellationToken = default)
    {
        return PostAsync<
            AdminPropertyTypeDto,
            SavePropertyTypeRequestDto>(
                "api/admin/property-types",
                request,
                "Không thể tạo loại hình nhà trọ.",
                cancellationToken);
    }


    public Task<ApiResult<AdminPropertyTypeDto>>
        UpdatePropertyTypeAsync(
            int propertyTypeId,
            SavePropertyTypeRequestDto request,
            CancellationToken cancellationToken = default)
    {
        return PutAsync<
            AdminPropertyTypeDto,
            SavePropertyTypeRequestDto>(
                $"api/admin/property-types/{propertyTypeId}",
                request,
                "Không thể cập nhật loại hình nhà trọ.",
                cancellationToken);
    }


    public Task<ApiResult<AdminPropertyTypeDto>>
        ActivatePropertyTypeAsync(
            int propertyTypeId,
            CancellationToken cancellationToken = default)
    {
        return PatchAsync<AdminPropertyTypeDto>(
            $"api/admin/property-types/{propertyTypeId}/activate",
            "Không thể kích hoạt loại hình nhà trọ.",
            cancellationToken);
    }


    public Task<ApiResult<AdminPropertyTypeDto>>
        DeactivatePropertyTypeAsync(
            int propertyTypeId,
            CancellationToken cancellationToken = default)
    {
        return PatchAsync<AdminPropertyTypeDto>(
            $"api/admin/property-types/{propertyTypeId}/deactivate",
            "Không thể vô hiệu hóa loại hình nhà trọ.",
            cancellationToken);
    }


    // ============================================================
    // AMENITIES
    // ============================================================

    public Task<ApiResult<List<AdminAmenityDto>>>
        GetAmenitiesAsync(
            CancellationToken cancellationToken = default)
    {
        return GetListAsync<AdminAmenityDto>(
            "api/admin/amenities",
            "Không thể tải danh sách tiện ích.",
            cancellationToken);
    }


    public Task<ApiResult<AdminAmenityDto>>
        CreateAmenityAsync(
            SaveAmenityRequestDto request,
            CancellationToken cancellationToken = default)
    {
        return PostAsync<
            AdminAmenityDto,
            SaveAmenityRequestDto>(
                "api/admin/amenities",
                request,
                "Không thể tạo tiện ích.",
                cancellationToken);
    }


    public Task<ApiResult<AdminAmenityDto>>
        UpdateAmenityAsync(
            int amenityId,
            SaveAmenityRequestDto request,
            CancellationToken cancellationToken = default)
    {
        return PutAsync<
            AdminAmenityDto,
            SaveAmenityRequestDto>(
                $"api/admin/amenities/{amenityId}",
                request,
                "Không thể cập nhật tiện ích.",
                cancellationToken);
    }


    public Task<ApiResult<AdminAmenityDto>>
        ActivateAmenityAsync(
            int amenityId,
            CancellationToken cancellationToken = default)
    {
        return PatchAsync<AdminAmenityDto>(
            $"api/admin/amenities/{amenityId}/activate",
            "Không thể kích hoạt tiện ích.",
            cancellationToken);
    }


    public Task<ApiResult<AdminAmenityDto>>
        DeactivateAmenityAsync(
            int amenityId,
            CancellationToken cancellationToken = default)
    {
        return PatchAsync<AdminAmenityDto>(
            $"api/admin/amenities/{amenityId}/deactivate",
            "Không thể vô hiệu hóa tiện ích.",
            cancellationToken);
    }


    // ============================================================
    // UNIVERSITIES
    // ============================================================

    public Task<ApiResult<List<AdminUniversityDto>>>
        GetUniversitiesAsync(
            CancellationToken cancellationToken = default)
    {
        return GetListAsync<AdminUniversityDto>(
            "api/admin/universities",
            "Không thể tải danh sách trường đại học.",
            cancellationToken);
    }


    public Task<ApiResult<AdminUniversityDto>>
        CreateUniversityAsync(
            SaveUniversityRequestDto request,
            CancellationToken cancellationToken = default)
    {
        return PostAsync<
            AdminUniversityDto,
            SaveUniversityRequestDto>(
                "api/admin/universities",
                request,
                "Không thể tạo trường đại học.",
                cancellationToken);
    }


    public Task<ApiResult<AdminUniversityDto>>
        UpdateUniversityAsync(
            long universityId,
            SaveUniversityRequestDto request,
            CancellationToken cancellationToken = default)
    {
        return PutAsync<
            AdminUniversityDto,
            SaveUniversityRequestDto>(
                $"api/admin/universities/{universityId}",
                request,
                "Không thể cập nhật trường đại học.",
                cancellationToken);
    }


    public Task<ApiResult<AdminUniversityDto>>
        ActivateUniversityAsync(
            long universityId,
            CancellationToken cancellationToken = default)
    {
        return PatchAsync<AdminUniversityDto>(
            $"api/admin/universities/{universityId}/activate",
            "Không thể kích hoạt trường đại học.",
            cancellationToken);
    }


    public Task<ApiResult<AdminUniversityDto>>
        DeactivateUniversityAsync(
            long universityId,
            CancellationToken cancellationToken = default)
    {
        return PatchAsync<AdminUniversityDto>(
            $"api/admin/universities/{universityId}/deactivate",
            "Không thể vô hiệu hóa trường đại học.",
            cancellationToken);
    }


    // ============================================================
    // GET LIST
    // ============================================================

    private async Task<ApiResult<List<T>>>
        GetListAsync<T>(
            string url,
            string defaultMessage,
            CancellationToken cancellationToken)
    {
        try
        {
            var client =
                CreateClient();


            var response =
                await client.GetAsync(
                    url,
                    cancellationToken);


            if (response.IsSuccessStatusCode)
            {
                var data =
                    await response.Content
                        .ReadFromJsonAsync<List<T>>(
                            cancellationToken:
                                cancellationToken);


                return data is not null

                    ? ApiResult<List<T>>
                        .Success(data)

                    : ApiResult<List<T>>
                        .Failure(
                            "Roomify.API trả về dữ liệu không hợp lệ.");
            }


            return await ReadFailureAsync<List<T>>(
                response,
                defaultMessage,
                cancellationToken);
        }
        catch (HttpRequestException ex)
        {
            _logger.LogWarning(
                ex,
                "Không thể kết nối Roomify.API. URL: {Url}",
                url);


            return ApiResult<List<T>>
                .Failure(
                    "Không thể kết nối tới Roomify.API.");
        }
        catch (TaskCanceledException ex)
        {
            _logger.LogWarning(
                ex,
                "Roomify.API phản hồi quá thời gian. URL: {Url}",
                url);


            return ApiResult<List<T>>
                .Failure(
                    "Roomify.API phản hồi quá thời gian.");
        }
        catch (Exception ex)
        {
            _logger.LogError(
                ex,
                "Lỗi khi gọi Roomify.API. URL: {Url}",
                url);


            return ApiResult<List<T>>
                .Failure(defaultMessage);
        }
    }


    // ============================================================
    // POST
    // ============================================================

    private async Task<ApiResult<TResponse>>
        PostAsync<TResponse, TRequest>(
            string url,
            TRequest request,
            string defaultMessage,
            CancellationToken cancellationToken)
    {
        try
        {
            var client =
                CreateClient();


            var response =
                await client.PostAsJsonAsync(
                    url,
                    request,
                    cancellationToken);


            return await ReadResponseAsync<TResponse>(
                response,
                defaultMessage,
                cancellationToken);
        }
        catch (HttpRequestException ex)
        {
            _logger.LogWarning(
                ex,
                "Không thể kết nối API khi POST {Url}.",
                url);


            return ApiResult<TResponse>
                .Failure(
                    "Không thể kết nối tới Roomify.API.");
        }
        catch (TaskCanceledException ex)
        {
            _logger.LogWarning(
                ex,
                "POST {Url} quá thời gian.",
                url);


            return ApiResult<TResponse>
                .Failure(
                    "Roomify.API phản hồi quá thời gian.");
        }
        catch (Exception ex)
        {
            _logger.LogError(
                ex,
                "Lỗi khi POST {Url}.",
                url);


            return ApiResult<TResponse>
                .Failure(defaultMessage);
        }
    }


    // ============================================================
    // PUT
    // ============================================================

    private async Task<ApiResult<TResponse>>
        PutAsync<TResponse, TRequest>(
            string url,
            TRequest request,
            string defaultMessage,
            CancellationToken cancellationToken)
    {
        try
        {
            var client =
                CreateClient();


            var response =
                await client.PutAsJsonAsync(
                    url,
                    request,
                    cancellationToken);


            return await ReadResponseAsync<TResponse>(
                response,
                defaultMessage,
                cancellationToken);
        }
        catch (HttpRequestException ex)
        {
            _logger.LogWarning(
                ex,
                "Không thể kết nối API khi PUT {Url}.",
                url);


            return ApiResult<TResponse>
                .Failure(
                    "Không thể kết nối tới Roomify.API.");
        }
        catch (TaskCanceledException ex)
        {
            _logger.LogWarning(
                ex,
                "PUT {Url} quá thời gian.",
                url);


            return ApiResult<TResponse>
                .Failure(
                    "Roomify.API phản hồi quá thời gian.");
        }
        catch (Exception ex)
        {
            _logger.LogError(
                ex,
                "Lỗi khi PUT {Url}.",
                url);


            return ApiResult<TResponse>
                .Failure(defaultMessage);
        }
    }


    // ============================================================
    // PATCH WITHOUT BODY
    // ============================================================

    private async Task<ApiResult<TResponse>>
        PatchAsync<TResponse>(
            string url,
            string defaultMessage,
            CancellationToken cancellationToken)
    {
        try
        {
            var client =
                CreateClient();


            using var request =
                new HttpRequestMessage(
                    HttpMethod.Patch,
                    url);


            var response =
                await client.SendAsync(
                    request,
                    cancellationToken);


            return await ReadResponseAsync<TResponse>(
                response,
                defaultMessage,
                cancellationToken);
        }
        catch (HttpRequestException ex)
        {
            _logger.LogWarning(
                ex,
                "Không thể kết nối API khi PATCH {Url}.",
                url);


            return ApiResult<TResponse>
                .Failure(
                    "Không thể kết nối tới Roomify.API.");
        }
        catch (TaskCanceledException ex)
        {
            _logger.LogWarning(
                ex,
                "PATCH {Url} quá thời gian.",
                url);


            return ApiResult<TResponse>
                .Failure(
                    "Roomify.API phản hồi quá thời gian.");
        }
        catch (Exception ex)
        {
            _logger.LogError(
                ex,
                "Lỗi khi PATCH {Url}.",
                url);


            return ApiResult<TResponse>
                .Failure(defaultMessage);
        }
    }


    // ============================================================
    // RESPONSE
    // ============================================================

    private static async Task<ApiResult<T>>
        ReadResponseAsync<T>(
            HttpResponseMessage response,
            string defaultMessage,
            CancellationToken cancellationToken)
    {
        if (response.IsSuccessStatusCode)
        {
            var data =
                await response.Content
                    .ReadFromJsonAsync<T>(
                        cancellationToken:
                            cancellationToken);


            if (data is null)
            {
                return ApiResult<T>.Failure(
                    "Roomify.API trả về dữ liệu không hợp lệ.");
            }


            return ApiResult<T>.Success(
                data);
        }


        return await ReadFailureAsync<T>(
            response,
            defaultMessage,
            cancellationToken);
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
            // Response lỗi không đúng JSON chuẩn.
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

    // ============================================================
    // LOCATION CATALOG
    // ============================================================

    public Task<ApiResult<List<ProvinceDto>>>
        GetProvincesAsync(
            CancellationToken cancellationToken = default)
    {
        return GetListAsync<ProvinceDto>(
            "api/catalog/provinces",
            "Không thể tải danh sách tỉnh/thành phố.",
            cancellationToken);
    }


    public Task<ApiResult<List<WardDto>>>
        GetWardsByProvinceAsync(
            int provinceId,
            CancellationToken cancellationToken = default)
    {
        return GetListAsync<WardDto>(
            $"api/catalog/provinces/{provinceId}/wards",
            "Không thể tải danh sách phường/xã.",
            cancellationToken);
    }
}