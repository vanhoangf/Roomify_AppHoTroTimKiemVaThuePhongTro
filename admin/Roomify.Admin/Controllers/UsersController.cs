using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

using Roomify.Admin.Models.Users;
using Roomify.Admin.Services.Dashboard;
using Roomify.Admin.Services.Users;

namespace Roomify.Admin.Controllers;

[Authorize(Roles = "ADMIN")]
public class UsersController : Controller
{
    private readonly IAdminUserApiService
        _userService;

    private readonly IAdminDashboardApiService
        _dashboardService;


    public UsersController(
        IAdminUserApiService userService,
        IAdminDashboardApiService dashboardService)
    {
        _userService = userService;
        _dashboardService = dashboardService;
    }


    [HttpGet]
    public async Task<IActionResult> Index(
        string? search = null,
        string? role = null,
        string? status = null,
        bool? verified = null,
        int page = 1,
        int pageSize = 8,
        CancellationToken cancellationToken = default)
    {
        page =
            Math.Max(
                page,
                1);

        pageSize =
            pageSize is 8 or 10 or 20 or 50
                ? pageSize
                : 8;


        var usersTask =
            _userService.GetUsersAsync(
                role,
                status,
                verified,
                search,
                page,
                pageSize,
                cancellationToken);


        var dashboardTask =
            _dashboardService.GetDashboardAsync(
                cancellationToken);


        await Task.WhenAll(
            usersTask,
            dashboardTask);


        var usersResult =
            await usersTask;

        var dashboardResult =
            await dashboardTask;


        // =====================================================
        // TOKEN KHÔNG HỢP LỆ
        // =====================================================

        if ((!usersResult.IsSuccess &&
             usersResult.StatusCode == 401) ||
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


        var model =
            new AdminUsersViewModel
            {
                Search = search?.Trim(),

                Role = role,

                Status = status,

                Verified = verified,

                Page = page,

                PageSize = pageSize
            };


        // =====================================================
        // USER LIST
        // =====================================================

        if (usersResult.IsSuccess &&
            usersResult.Data is not null)
        {
            model.Users =
                usersResult.Data;
        }
        else
        {
            model.ErrorMessage =
                usersResult.Message
                ?? "Không thể tải danh sách người dùng.";
        }


        // =====================================================
        // KPI
        // =====================================================

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
            await _userService.GetUserByIdAsync(
                id,
                cancellationToken);


        // =====================================================
        // TOKEN HẾT HẠN
        // =====================================================

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
            new AdminUserDetailViewModel
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
                ?? "Không thể tải thông tin người dùng.";

            return View(model);
        }


        model.User =
            result.Data;

        return View(model);
    }

    // ============================================================
    // LOCK USER
    // ============================================================

    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Lock(
        long id,
        string reason,
        string? returnUrl = null,
        CancellationToken cancellationToken = default)
    {
        if (id <= 0)
        {
            return RedirectToAction(nameof(Index));
        }

        if (string.IsNullOrWhiteSpace(reason))
        {
            TempData["ErrorMessage"] =
                "Vui lòng nhập lý do khóa tài khoản.";

            return RedirectToAction(
                nameof(Details),
                new
                {
                    id,
                    returnUrl
                });
        }

        var result =
            await _userService.LockUserAsync(
                id,
                reason,
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
                ?? "Không thể khóa tài khoản.";

            return RedirectToAction(
                nameof(Details),
                new
                {
                    id,
                    returnUrl
                });
        }

        TempData["SuccessMessage"] =
            "Đã khóa tài khoản thành công.";

        return RedirectToAction(
            nameof(Details),
            new
            {
                id,
                returnUrl
            });
    }


    // ============================================================
    // UNLOCK USER
    // ============================================================

    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Unlock(
        long id,
        string? returnUrl = null,
        CancellationToken cancellationToken = default)
    {
        if (id <= 0)
        {
            return RedirectToAction(nameof(Index));
        }

        var result =
            await _userService.UnlockUserAsync(
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

        if (!result.IsSuccess)
        {
            TempData["ErrorMessage"] =
                result.Message
                ?? "Không thể mở khóa tài khoản.";

            return RedirectToAction(
                nameof(Details),
                new
                {
                    id,
                    returnUrl
                });
        }

        TempData["SuccessMessage"] =
            "Đã mở khóa tài khoản thành công.";

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
    public async Task<IActionResult> VerifyStudent(
        long id,
        long studentId,
        string? returnUrl = null,
        CancellationToken cancellationToken = default)
    {
        if (id <= 0 ||
            studentId <= 0)
        {
            TempData["ErrorMessage"] =
                "Thông tin sinh viên không hợp lệ.";

            return RedirectToAction(
                nameof(Details),
                new
                {
                    id,
                    returnUrl
                });
        }

        var result =
            await _userService.VerifyStudentAsync(
                studentId,
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
                ?? "Không thể xác minh sinh viên.";

            return RedirectToAction(
                nameof(Details),
                new
                {
                    id,
                    returnUrl
                });
        }

        TempData["SuccessMessage"] =
            "Đã xác minh hồ sơ sinh viên thành công.";

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
    public async Task<IActionResult> VerifyLandlord(
        long id,
        long landlordId,
        string? returnUrl = null,
        CancellationToken cancellationToken = default)
    {
        if (id <= 0 ||
            landlordId <= 0)
        {
            TempData["ErrorMessage"] =
                "Thông tin chủ trọ không hợp lệ.";

            return RedirectToAction(
                nameof(Details),
                new
                {
                    id,
                    returnUrl
                });
        }

        var result =
            await _userService.VerifyLandlordAsync(
                landlordId,
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
                ?? "Không thể xác minh chủ trọ.";

            return RedirectToAction(
                nameof(Details),
                new
                {
                    id,
                    returnUrl
                });
        }

        TempData["SuccessMessage"] =
            "Đã xác minh hồ sơ chủ trọ thành công.";

        return RedirectToAction(
            nameof(Details),
            new
            {
                id,
                returnUrl
            });
    }
}