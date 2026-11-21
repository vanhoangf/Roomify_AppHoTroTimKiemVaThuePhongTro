using Roomify.Admin.Models.Catalog;
using Roomify.Admin.Models.Common;

namespace Roomify.Admin.Services.Catalog;

public interface IAdminCatalogApiService
{
    // ============================================================
    // PROPERTY TYPES
    // ============================================================

    Task<ApiResult<List<AdminPropertyTypeDto>>>
        GetPropertyTypesAsync(
            CancellationToken cancellationToken = default);

    Task<ApiResult<AdminPropertyTypeDto>>
        CreatePropertyTypeAsync(
            SavePropertyTypeRequestDto request,
            CancellationToken cancellationToken = default);

    Task<ApiResult<AdminPropertyTypeDto>>
        UpdatePropertyTypeAsync(
            int propertyTypeId,
            SavePropertyTypeRequestDto request,
            CancellationToken cancellationToken = default);

    Task<ApiResult<AdminPropertyTypeDto>>
        ActivatePropertyTypeAsync(
            int propertyTypeId,
            CancellationToken cancellationToken = default);

    Task<ApiResult<AdminPropertyTypeDto>>
        DeactivatePropertyTypeAsync(
            int propertyTypeId,
            CancellationToken cancellationToken = default);


    // ============================================================
    // AMENITIES
    // ============================================================

    Task<ApiResult<List<AdminAmenityDto>>>
        GetAmenitiesAsync(
            CancellationToken cancellationToken = default);

    Task<ApiResult<AdminAmenityDto>>
        CreateAmenityAsync(
            SaveAmenityRequestDto request,
            CancellationToken cancellationToken = default);

    Task<ApiResult<AdminAmenityDto>>
        UpdateAmenityAsync(
            int amenityId,
            SaveAmenityRequestDto request,
            CancellationToken cancellationToken = default);

    Task<ApiResult<AdminAmenityDto>>
        ActivateAmenityAsync(
            int amenityId,
            CancellationToken cancellationToken = default);

    Task<ApiResult<AdminAmenityDto>>
        DeactivateAmenityAsync(
            int amenityId,
            CancellationToken cancellationToken = default);


    // ============================================================
    // UNIVERSITIES
    // ============================================================

    Task<ApiResult<List<AdminUniversityDto>>>
        GetUniversitiesAsync(
            CancellationToken cancellationToken = default);

    Task<ApiResult<AdminUniversityDto>>
        CreateUniversityAsync(
            SaveUniversityRequestDto request,
            CancellationToken cancellationToken = default);

    Task<ApiResult<AdminUniversityDto>>
        UpdateUniversityAsync(
            long universityId,
            SaveUniversityRequestDto request,
            CancellationToken cancellationToken = default);

    Task<ApiResult<AdminUniversityDto>>
        ActivateUniversityAsync(
            long universityId,
            CancellationToken cancellationToken = default);

    Task<ApiResult<AdminUniversityDto>>
        DeactivateUniversityAsync(
            long universityId,
            CancellationToken cancellationToken = default);

    // ============================================================
    // LOCATION CATALOG
    // ============================================================

    Task<ApiResult<List<ProvinceDto>>>
        GetProvincesAsync(
            CancellationToken cancellationToken = default);

    Task<ApiResult<List<WardDto>>>
        GetWardsByProvinceAsync(
            int provinceId,
            CancellationToken cancellationToken = default);
}