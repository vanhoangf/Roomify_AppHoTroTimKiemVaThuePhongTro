using Roomify.API.DTOs.Catalog;

namespace Roomify.API.Services.Interfaces;

public interface ICatalogService
{
    Task<IReadOnlyList<ProvinceResponse>> GetProvincesAsync(
        CancellationToken cancellationToken = default);

    Task<IReadOnlyList<WardResponse>> GetWardsByProvinceAsync(
        int provinceId,
        CancellationToken cancellationToken = default);

    Task<IReadOnlyList<UniversityResponse>> GetUniversitiesAsync(
        int? provinceId,
        int? wardId,
        string? keyword,
        CancellationToken cancellationToken = default);

    Task<IReadOnlyList<PropertyTypeResponse>> GetPropertyTypesAsync(
        CancellationToken cancellationToken = default);

    Task<IReadOnlyList<AmenityResponse>> GetAmenitiesAsync(
        CancellationToken cancellationToken = default);
}