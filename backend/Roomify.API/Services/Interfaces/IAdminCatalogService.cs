using Roomify.API.DTOs.Admin.Catalog;

namespace Roomify.API.Services.Interfaces;

public interface IAdminCatalogService
{
    // ============================================================
    // PROPERTY TYPES
    // ============================================================

    Task<List<AdminPropertyTypeResponse>>
        GetPropertyTypesAsync();

    Task<AdminPropertyTypeResponse>
        CreatePropertyTypeAsync(
            SavePropertyTypeRequest request);

    Task<AdminPropertyTypeResponse>
        UpdatePropertyTypeAsync(
            int propertyTypeId,
            SavePropertyTypeRequest request);

    Task<AdminPropertyTypeResponse>
        ActivatePropertyTypeAsync(
            int propertyTypeId);

    Task<AdminPropertyTypeResponse>
        DeactivatePropertyTypeAsync(
            int propertyTypeId);


    // ============================================================
    // AMENITIES
    // ============================================================

    Task<List<AdminAmenityResponse>>
        GetAmenitiesAsync();

    Task<AdminAmenityResponse>
        CreateAmenityAsync(
            SaveAmenityRequest request);

    Task<AdminAmenityResponse>
        UpdateAmenityAsync(
            int amenityId,
            SaveAmenityRequest request);

    Task<AdminAmenityResponse>
        ActivateAmenityAsync(
            int amenityId);

    Task<AdminAmenityResponse>
        DeactivateAmenityAsync(
            int amenityId);


    // ============================================================
    // UNIVERSITIES
    // ============================================================

    Task<List<AdminUniversityResponse>>
        GetUniversitiesAsync();

    Task<AdminUniversityResponse>
        CreateUniversityAsync(
            SaveUniversityRequest request);

    Task<AdminUniversityResponse>
        UpdateUniversityAsync(
            long universityId,
            SaveUniversityRequest request);

    Task<AdminUniversityResponse>
        ActivateUniversityAsync(
            long universityId);

    Task<AdminUniversityResponse>
        DeactivateUniversityAsync(
            long universityId);
}