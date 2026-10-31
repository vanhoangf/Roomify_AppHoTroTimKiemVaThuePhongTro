using Microsoft.AspNetCore.Mvc;

using Roomify.API.Services.Interfaces;

namespace Roomify.API.Controllers;

[ApiController]
[Route("api/catalog")]
public class CatalogController : ControllerBase
{
    private readonly ICatalogService _catalogService;

    public CatalogController(
        ICatalogService catalogService)
    {
        _catalogService = catalogService;
    }

    [HttpGet("provinces")]
    public async Task<IActionResult> GetProvinces(
        CancellationToken cancellationToken)
    {
        var result =
            await _catalogService
                .GetProvincesAsync(
                    cancellationToken);

        return Ok(result);
    }

    [HttpGet("provinces/{provinceId:int}/wards")]
    public async Task<IActionResult> GetWards(
        int provinceId,
        CancellationToken cancellationToken)
    {
        var result =
            await _catalogService
                .GetWardsByProvinceAsync(
                    provinceId,
                    cancellationToken);

        return Ok(result);
    }

    [HttpGet("universities")]
    public async Task<IActionResult> GetUniversities(
        [FromQuery] int? provinceId,
        [FromQuery] int? wardId,
        [FromQuery] string? keyword,
        CancellationToken cancellationToken)
    {
        var result =
            await _catalogService
                .GetUniversitiesAsync(
                    provinceId,
                    wardId,
                    keyword,
                    cancellationToken);

        return Ok(result);
    }

    [HttpGet("property-types")]
    public async Task<IActionResult> GetPropertyTypes(
        CancellationToken cancellationToken)
    {
        var result =
            await _catalogService
                .GetPropertyTypesAsync(
                    cancellationToken);

        return Ok(result);
    }

    [HttpGet("amenities")]
    public async Task<IActionResult> GetAmenities(
        CancellationToken cancellationToken)
    {
        var result =
            await _catalogService
                .GetAmenitiesAsync(
                    cancellationToken);

        return Ok(result);
    }
}