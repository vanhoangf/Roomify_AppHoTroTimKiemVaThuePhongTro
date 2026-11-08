using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Roomify.API.DTOs.Admin.Reports;
using Roomify.API.Helpers;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Controllers;

[ApiController]
[Route("api/admin/reports")]
[Authorize(Roles = "ADMIN")]
public class AdminReportsController : ControllerBase
{
    private readonly IAdminReportService _adminReportService;

    public AdminReportsController(
        IAdminReportService adminReportService)
    {
        _adminReportService = adminReportService;
    }

    // ============================================================
    // GET LIST
    // GET /api/admin/reports
    // ============================================================

    [HttpGet]
    public async Task<ActionResult<AdminReportListResponse>> GetReports(
        [FromQuery] string? status = null,
        [FromQuery] string? search = null,
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 10)
    {
        var result =
            await _adminReportService.GetReportsAsync(
                status,
                search,
                page,
                pageSize);

        return Ok(result);
    }

    // ============================================================
    // GET DETAIL
    // GET /api/admin/reports/{reportId}
    // ============================================================

    [HttpGet("{reportId:long}")]
    public async Task<ActionResult<AdminReportResponse>> GetReportById(
        long reportId)
    {
        var result =
            await _adminReportService.GetReportByIdAsync(
                reportId);

        return Ok(result);
    }

    // ============================================================
    // RESOLVE
    // PATCH /api/admin/reports/{reportId}/resolve
    // ============================================================

    [HttpPatch("{reportId:long}/resolve")]
    public async Task<ActionResult<AdminReportResponse>> ResolveReport(
        long reportId,
        [FromBody] AdminReportDecisionRequest request)
    {
        var adminUserId =
            User.GetRequiredUserId();

        var result =
            await _adminReportService.ResolveAsync(
                adminUserId,
                reportId,
                request);

        return Ok(result);
    }

    // ============================================================
    // REJECT
    // PATCH /api/admin/reports/{reportId}/reject
    // ============================================================

    [HttpPatch("{reportId:long}/reject")]
    public async Task<ActionResult<AdminReportResponse>> RejectReport(
        long reportId,
        [FromBody] AdminReportDecisionRequest request)
    {
        var adminUserId =
            User.GetRequiredUserId();

        var result =
            await _adminReportService.RejectAsync(
                adminUserId,
                reportId,
                request);

        return Ok(result);
    }
}