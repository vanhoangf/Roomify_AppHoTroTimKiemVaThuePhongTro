using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Roomify.API.DTOs.Reports;
using Roomify.API.Helpers;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Controllers;

[ApiController]
public class ReportsController : ControllerBase
{
    private readonly IReportService _reportService;

    public ReportsController(IReportService reportService)
    {
        _reportService = reportService;
    }

    // ============================================================
    // USER - CREATE REPORT
    // ============================================================

    // POST:
    // /api/rooms/{roomId}/reports
    //
    // User đăng nhập có thể báo cáo một phòng.
    [HttpPost("api/rooms/{roomId:long}/reports")]
    [Authorize]
    public async Task<ActionResult<ReportResponse>> CreateReport(
        long roomId,
        [FromBody] CreateReportRequest request)
    {
        var userId = User.GetRequiredUserId();

        var result = await _reportService.CreateAsync(
            userId,
            roomId,
            request);

        return StatusCode(
            StatusCodes.Status201Created,
            result);
    }

    // ============================================================
    // USER - GET OWN REPORTS
    // ============================================================

    // GET:
    // /api/reports/me?status=OPEN&page=1&pageSize=10
    //
    // User xem danh sách các báo cáo do chính mình tạo.
    [HttpGet("api/reports/me")]
    [Authorize]
    public async Task<ActionResult<ReportListResponse>> GetMyReports(
        [FromQuery] string? status = null,
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 10)
    {
        var userId = User.GetRequiredUserId();

        var result = await _reportService.GetMyReportsAsync(
            userId,
            status,
            page,
            pageSize);

        return Ok(result);
    }

    // ============================================================
    // USER - GET OWN REPORT DETAIL
    // ============================================================

    // GET:
    // /api/reports/me/{reportId}
    //
    // User chỉ xem được báo cáo do chính mình tạo.
    [HttpGet("api/reports/me/{reportId:long}")]
    [Authorize]
    public async Task<ActionResult<ReportResponse>> GetMyReportById(
        long reportId)
    {
        var userId = User.GetRequiredUserId();

        var result = await _reportService.GetMyReportByIdAsync(
            userId,
            reportId);

        return Ok(result);
    }
}