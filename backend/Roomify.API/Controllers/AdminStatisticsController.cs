using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

using Roomify.API.DTOs.Admin.Statistics;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Controllers;

[ApiController]
[Route("api/admin/statistics")]
[Authorize(Roles = "ADMIN")]
public class AdminStatisticsController
    : ControllerBase
{
    private readonly IAdminStatisticsService
        _statisticsService;


    public AdminStatisticsController(
        IAdminStatisticsService statisticsService)
    {
        _statisticsService =
            statisticsService;
    }

    [HttpGet]
    public async Task<ActionResult<AdminStatisticsResponse>>
        GetStatistics(
            [FromQuery] DateOnly? fromDate = null,
            [FromQuery] DateOnly? toDate = null)
    {
        var result =
            await _statisticsService
                .GetStatisticsAsync(
                    fromDate,
                    toDate);


        return Ok(
            result);
    }
}