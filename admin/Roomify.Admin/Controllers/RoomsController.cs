using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

using Roomify.Admin.Models.Rooms;
using Roomify.Admin.Services.Dashboard;
using Roomify.Admin.Services.Rooms;

namespace Roomify.Admin.Controllers;

[Authorize(Roles = "ADMIN")]
public class RoomsController : Controller
{
    private readonly IAdminRoomApiService
        _roomService;

    private readonly IAdminDashboardApiService
        _dashboardService;


    public RoomsController(
        IAdminRoomApiService roomService,
        IAdminDashboardApiService dashboardService)
    {
        _roomService =
            roomService;

        _dashboardService =
            dashboardService;
    }


    [HttpGet]
    public async Task<IActionResult> Index(
        string? search = null,
        string? listingStatus = null,
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


        var roomsTask =
            _roomService.GetRoomsAsync(
                listingStatus,
                search,
                page,
                pageSize,
                cancellationToken);


        var dashboardTask =
            _dashboardService.GetDashboardAsync(
                cancellationToken);


        await Task.WhenAll(
            roomsTask,
            dashboardTask);


        var roomsResult =
            await roomsTask;

        var dashboardResult =
            await dashboardTask;

        if ((!roomsResult.IsSuccess &&
             roomsResult.StatusCode == 401) ||
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
            new AdminRoomsViewModel
            {
                Search =
                    search?.Trim(),

                ListingStatus =
                    listingStatus,

                Page =
                    page,

                PageSize =
                    pageSize
            };

        if (roomsResult.IsSuccess &&
            roomsResult.Data is not null)
        {
            model.Rooms =
                roomsResult.Data;
        }
        else
        {
            model.ErrorMessage =
                roomsResult.Message
                ?? "Không thể tải danh sách phòng.";
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
            await _roomService.GetRoomByIdAsync(
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
            new AdminRoomDetailViewModel
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
                ?? "Không thể tải thông tin phòng.";

            return View(model);
        }


        model.Room =
            result.Data;

        return View(model);
    }

    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Approve(
        long id,
        string? returnUrl = null,
        CancellationToken cancellationToken = default)
    {
        if (id <= 0)
        {
            return RedirectToAction(nameof(Index));
        }

        var result =
            await _roomService.ApproveRoomAsync(
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
                ?? "Không thể duyệt tin phòng.";

            return RedirectToAction(
                nameof(Details),
                new
                {
                    id,
                    returnUrl
                });
        }


        TempData["SuccessMessage"] =
            "Tin phòng đã được duyệt và đăng thành công.";

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
        string? reason,
        string? returnUrl = null,
        CancellationToken cancellationToken = default)
    {
        if (id <= 0)
        {
            return RedirectToAction(nameof(Index));
        }


        var normalizedReason =
            reason?.Trim();


        if (string.IsNullOrWhiteSpace(normalizedReason))
        {
            TempData["ErrorMessage"] =
                "Vui lòng nhập lý do từ chối tin phòng.";

            return RedirectToAction(
                nameof(Details),
                new
                {
                    id,
                    returnUrl
                });
        }


        if (normalizedReason.Length > 1000)
        {
            TempData["ErrorMessage"] =
                "Lý do từ chối không được vượt quá 1000 ký tự.";

            return RedirectToAction(
                nameof(Details),
                new
                {
                    id,
                    returnUrl
                });
        }


        var result =
            await _roomService.RejectRoomAsync(
                id,
                normalizedReason,
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
                ?? "Không thể từ chối tin phòng.";

            return RedirectToAction(
                nameof(Details),
                new
                {
                    id,
                    returnUrl
                });
        }


        TempData["SuccessMessage"] =
            "Đã từ chối tin phòng.";

        return RedirectToAction(
            nameof(Details),
            new
            {
                id,
                returnUrl
            });
    }
}