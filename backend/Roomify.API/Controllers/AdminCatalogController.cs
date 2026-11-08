using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Roomify.API.DTOs.Admin.Catalog;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Controllers;

[ApiController]
[Route("api/admin")]
[Authorize(Roles = "ADMIN")]
public class AdminCatalogController : ControllerBase
{
    private readonly IAdminCatalogService _adminCatalogService;

    public AdminCatalogController(
        IAdminCatalogService adminCatalogService)
    {
        _adminCatalogService = adminCatalogService;
    }

    // ============================================================
    // PROPERTY TYPES
    // ============================================================

    [HttpGet("property-types")]
    public async Task<ActionResult<List<AdminPropertyTypeResponse>>>
        GetPropertyTypes()
    {
        var result =
            await _adminCatalogService.GetPropertyTypesAsync();

        return Ok(result);
    }

    [HttpPost("property-types")]
    public async Task<ActionResult<AdminPropertyTypeResponse>>
        CreatePropertyType(
            [FromBody] SavePropertyTypeRequest request)
    {
        var result =
            await _adminCatalogService.CreatePropertyTypeAsync(
                request);

        return StatusCode(
            StatusCodes.Status201Created,
            result);
    }

    [HttpPut("property-types/{propertyTypeId:int}")]
    public async Task<ActionResult<AdminPropertyTypeResponse>>
        UpdatePropertyType(
            int propertyTypeId,
            [FromBody] SavePropertyTypeRequest request)
    {
        var result =
            await _adminCatalogService.UpdatePropertyTypeAsync(
                propertyTypeId,
                request);

        return Ok(result);
    }

    [HttpPatch("property-types/{propertyTypeId:int}/activate")]
    public async Task<ActionResult<AdminPropertyTypeResponse>>
        ActivatePropertyType(
            int propertyTypeId)
    {
        var result =
            await _adminCatalogService.ActivatePropertyTypeAsync(
                propertyTypeId);

        return Ok(result);
    }

    [HttpPatch("property-types/{propertyTypeId:int}/deactivate")]
    public async Task<ActionResult<AdminPropertyTypeResponse>>
        DeactivatePropertyType(
            int propertyTypeId)
    {
        var result =
            await _adminCatalogService.DeactivatePropertyTypeAsync(
                propertyTypeId);

        return Ok(result);
    }


    // ============================================================
    // AMENITIES
    // ============================================================

    [HttpGet("amenities")]
    public async Task<ActionResult<List<AdminAmenityResponse>>>
        GetAmenities()
    {
        var result =
            await _adminCatalogService.GetAmenitiesAsync();

        return Ok(result);
    }

    [HttpPost("amenities")]
    public async Task<ActionResult<AdminAmenityResponse>>
        CreateAmenity(
            [FromBody] SaveAmenityRequest request)
    {
        var result =
            await _adminCatalogService.CreateAmenityAsync(
                request);

        return StatusCode(
            StatusCodes.Status201Created,
            result);
    }

    [HttpPut("amenities/{amenityId:int}")]
    public async Task<ActionResult<AdminAmenityResponse>>
        UpdateAmenity(
            int amenityId,
            [FromBody] SaveAmenityRequest request)
    {
        var result =
            await _adminCatalogService.UpdateAmenityAsync(
                amenityId,
                request);

        return Ok(result);
    }

    [HttpPatch("amenities/{amenityId:int}/activate")]
    public async Task<ActionResult<AdminAmenityResponse>>
        ActivateAmenity(
            int amenityId)
    {
        var result =
            await _adminCatalogService.ActivateAmenityAsync(
                amenityId);

        return Ok(result);
    }

    [HttpPatch("amenities/{amenityId:int}/deactivate")]
    public async Task<ActionResult<AdminAmenityResponse>>
        DeactivateAmenity(
            int amenityId)
    {
        var result =
            await _adminCatalogService.DeactivateAmenityAsync(
                amenityId);

        return Ok(result);
    }


    // ============================================================
    // UNIVERSITIES
    // ============================================================

    [HttpGet("universities")]
    public async Task<ActionResult<List<AdminUniversityResponse>>>
        GetUniversities()
    {
        var result =
            await _adminCatalogService.GetUniversitiesAsync();

        return Ok(result);
    }

    [HttpPost("universities")]
    public async Task<ActionResult<AdminUniversityResponse>>
        CreateUniversity(
            [FromBody] SaveUniversityRequest request)
    {
        var result =
            await _adminCatalogService.CreateUniversityAsync(
                request);

        return StatusCode(
            StatusCodes.Status201Created,
            result);
    }

    [HttpPut("universities/{universityId:long}")]
    public async Task<ActionResult<AdminUniversityResponse>>
        UpdateUniversity(
            long universityId,
            [FromBody] SaveUniversityRequest request)
    {
        var result =
            await _adminCatalogService.UpdateUniversityAsync(
                universityId,
                request);

        return Ok(result);
    }

    [HttpPatch("universities/{universityId:long}/activate")]
    public async Task<ActionResult<AdminUniversityResponse>>
        ActivateUniversity(
            long universityId)
    {
        var result =
            await _adminCatalogService.ActivateUniversityAsync(
                universityId);

        return Ok(result);
    }

    [HttpPatch("universities/{universityId:long}/deactivate")]
    public async Task<ActionResult<AdminUniversityResponse>>
        DeactivateUniversity(
            long universityId)
    {
        var result =
            await _adminCatalogService.DeactivateUniversityAsync(
                universityId);

        return Ok(result);
    }
}