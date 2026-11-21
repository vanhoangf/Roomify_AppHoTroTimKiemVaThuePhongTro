using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Roomify.Admin.Models.Dashboard;
using Roomify.Admin.Services.Dashboard;

namespace Roomify.Admin.Controllers;

[Authorize(Roles = "ADMIN")]
public class DashboardController : Controller
{
    private readonly IAdminDashboardApiService
        _dashboardService;


    public DashboardController(
        IAdminDashboardApiService dashboardService)
    {
        _dashboardService = dashboardService;
    }


    [HttpGet]
    public async Task<IActionResult> Index(
        CancellationToken cancellationToken)
    {
        var result =
            await _dashboardService
                .GetDashboardAsync(
                    cancellationToken);


        // JWT đã hết hạn / không hợp lệ.
        if (!result.IsSuccess &&
            result.StatusCode == 401)
        {
            HttpContext.Session.Clear();

            await HttpContext.SignOutAsync(
                CookieAuthenticationDefaults
                    .AuthenticationScheme);

            return RedirectToAction(
                "Login",
                "Auth");
        }


        var model =
            new DashboardViewModel();


        if (!result.IsSuccess ||
            result.Data is null)
        {
            model.ErrorMessage =
                result.Message
                ?? "Không thể tải Dashboard.";

            return View(model);
        }


        model.Data =
            result.Data;

        return View(model);
    }
}