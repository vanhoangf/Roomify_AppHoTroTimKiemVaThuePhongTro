using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Roomify.API.DTOs.Admin.Dashboard;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Controllers;

[ApiController]
[Route("api/admin/dashboard")]
[Authorize(Roles = "ADMIN")]
public class AdminDashboardController : ControllerBase
{
    private readonly IAdminDashboardService _adminDashboardService;

    public AdminDashboardController(
        IAdminDashboardService adminDashboardService)
    {
        _adminDashboardService = adminDashboardService;
    }

    // ============================================================
    // GET DASHBOARD
    // GET /api/admin/dashboard
    // ============================================================

    [HttpGet]
    public async Task<ActionResult<AdminDashboardResponse>>
        GetDashboard()
    {
        var result =
            await _adminDashboardService.GetDashboardAsync();

        return Ok(result);
    }
}