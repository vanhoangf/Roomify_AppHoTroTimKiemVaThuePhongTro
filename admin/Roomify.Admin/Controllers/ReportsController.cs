using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

using Roomify.Admin.Models.Reports;
using Roomify.Admin.Services.Dashboard;
using Roomify.Admin.Services.Reports;

namespace Roomify.Admin.Controllers;

[Authorize(Roles = "ADMIN")]
public class ReportsController : Controller
{
    private readonly IAdminReportApiService
        _reportService;

    private readonly IAdminDashboardApiService
        _dashboardService;


    public ReportsController(
        IAdminReportApiService reportService,
        IAdminDashboardApiService dashboardService)
    {
        _reportService =
            reportService;

        _dashboardService =
            dashboardService;
    }


    [HttpGet]
    public async Task<IActionResult> Index(
        string? search = null,
        string? status = null,
        int page = 1,
        int pageSize = 10,
        CancellationToken cancellationToken = default)
    {
        page =
            Math.Max(
                page,
                1);


        pageSize =
            pageSize is 10 or 20 or 50
                ? pageSize
                : 10;


        var reportsTask =
            _reportService.GetReportsAsync(
                status,
                search,
                page,
                pageSize,
                cancellationToken);


        var dashboardTask =
            _dashboardService.GetDashboardAsync(
                cancellationToken);


        await Task.WhenAll(
            reportsTask,
            dashboardTask);


        var reportsResult =
            await reportsTask;

        var dashboardResult =
            await dashboardTask;


        // ========================================================
        // UNAUTHORIZED
        // ========================================================

        if ((!reportsResult.IsSuccess &&
             reportsResult.StatusCode == 401) ||
            (!dashboardResult.IsSuccess &&
             dashboardResult.StatusCode == 401))
        {
            HttpContext.Session.Clear();

            await HttpContext.SignOutAsync(
                CookieAuthenticationDefaults
                    .AuthenticationScheme);


            return RedirectToAction(
                "Login",
                "Auth");
        }


        // ========================================================
        // VIEW MODEL
        // ========================================================

        var model =
            new AdminReportsViewModel
            {
                Search =
                    search?.Trim(),

                Status =
                    status?.Trim()
                        .ToUpperInvariant(),

                Page =
                    page,

                PageSize =
                    pageSize
            };


        if (reportsResult.IsSuccess &&
            reportsResult.Data is not null)
        {
            model.Reports =
                reportsResult.Data;
        }
        else
        {
            model.ErrorMessage =
                reportsResult.Message
                ?? "Không thể tải danh sách báo cáo.";
        }


        if (dashboardResult.IsSuccess &&
            dashboardResult.Data is not null)
        {
            model.Dashboard =
                dashboardResult.Data;
        }
        else
        {
            model.StatisticsErrorMessage =
                dashboardResult.Message;
        }


        return View(model);
    }

    [HttpGet]
    public async Task<IActionResult> Details(
    long id,
    string? returnUrl = null,
    CancellationToken cancellationToken = default)
    {
        if (id <= 0)
        {
            return RedirectToAction(
                nameof(Index));
        }


        var result =
            await _reportService.GetReportByIdAsync(
                id,
                cancellationToken);


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
            new AdminReportDetailViewModel
            {
                ReturnUrl =
                    !string.IsNullOrWhiteSpace(returnUrl) &&
                    Url.IsLocalUrl(returnUrl)
                        ? returnUrl
                        : Url.Action(nameof(Index))
            };


        if (!result.IsSuccess ||
            result.Data is null)
        {
            model.ErrorMessage =
                result.Message
                ?? "Không thể tải thông tin báo cáo.";

            return View(model);
        }


        model.Report =
            result.Data;

        return View(model);
    }

    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Resolve(
        long id,
        string? adminNote,
        string? returnUrl = null,
        CancellationToken cancellationToken = default)
    {
        if (id <= 0)
        {
            return RedirectToAction(
                nameof(Index));
        }


        var normalizedNote =
            adminNote?.Trim();


        if (string.IsNullOrWhiteSpace(normalizedNote))
        {
            TempData["ErrorMessage"] =
                "Vui lòng nhập ghi chú xử lý báo cáo.";

            return RedirectToAction(
                nameof(Details),
                new
                {
                    id,
                    returnUrl
                });
        }


        if (normalizedNote.Length > 2000)
        {
            TempData["ErrorMessage"] =
                "Ghi chú xử lý không được vượt quá 2000 ký tự.";

            return RedirectToAction(
                nameof(Details),
                new
                {
                    id,
                    returnUrl
                });
        }


        var result =
            await _reportService.ResolveReportAsync(
                id,
                normalizedNote,
                cancellationToken);


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


        if (!result.IsSuccess)
        {
            TempData["ErrorMessage"] =
                result.Message
                ?? "Không thể xử lý báo cáo.";

            return RedirectToAction(
                nameof(Details),
                new
                {
                    id,
                    returnUrl
                });
        }


        TempData["SuccessMessage"] =
            "Báo cáo đã được đánh dấu là đã xử lý.";

        return RedirectToAction(
            nameof(Details),
            new
            {
                id,
                returnUrl
            });
    }

    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Reject(
        long id,
        string? adminNote,
        string? returnUrl = null,
        CancellationToken cancellationToken = default)
    {
        if (id <= 0)
        {
            return RedirectToAction(
                nameof(Index));
        }


        var normalizedNote =
            adminNote?.Trim();


        if (string.IsNullOrWhiteSpace(normalizedNote))
        {
            TempData["ErrorMessage"] =
                "Vui lòng nhập lý do từ chối báo cáo.";

            return RedirectToAction(
                nameof(Details),
                new
                {
                    id,
                    returnUrl
                });
        }


        if (normalizedNote.Length > 2000)
        {
            TempData["ErrorMessage"] =
                "Lý do từ chối không được vượt quá 2000 ký tự.";

            return RedirectToAction(
                nameof(Details),
                new
                {
                    id,
                    returnUrl
                });
        }


        var result =
            await _reportService.RejectReportAsync(
                id,
                normalizedNote,
                cancellationToken);


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


        if (!result.IsSuccess)
        {
            TempData["ErrorMessage"] =
                result.Message
                ?? "Không thể từ chối báo cáo.";

            return RedirectToAction(
                nameof(Details),
                new
                {
                    id,
                    returnUrl
                });
        }


        TempData["SuccessMessage"] =
            "Báo cáo đã được từ chối.";

        return RedirectToAction(
            nameof(Details),
            new
            {
                id,
                returnUrl
            });
    }
}