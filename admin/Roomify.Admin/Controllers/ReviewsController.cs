using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

using Roomify.Admin.Models.Reviews;
using Roomify.Admin.Services.Reviews;

namespace Roomify.Admin.Controllers;

[Authorize(Roles = "ADMIN")]
public class ReviewsController : Controller
{
    private readonly IAdminReviewApiService
        _reviewService;


    public ReviewsController(
        IAdminReviewApiService reviewService)
    {
        _reviewService =
            reviewService;
    }


    [HttpGet]
    public async Task<IActionResult> Index(
        string? search = null,
        string? status = null,
        int? rating = null,
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


        var normalizedStatus =
            string.IsNullOrWhiteSpace(status)
                ? null
                : status.Trim()
                    .ToUpperInvariant();


        if (normalizedStatus is not null &&
            normalizedStatus is not "VISIBLE" and not "HIDDEN")
        {
            normalizedStatus =
                null;
        }


        if (rating.HasValue &&
            (rating.Value < 1 ||
             rating.Value > 5))
        {
            rating =
                null;
        }


        var result =
            await _reviewService.GetReviewsAsync(
                normalizedStatus,
                rating,
                search,
                page,
                pageSize,
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
            new AdminReviewsViewModel
            {
                Search =
                    search?.Trim(),

                Status =
                    normalizedStatus,

                Rating =
                    rating,

                Page =
                    page,

                PageSize =
                    pageSize
            };


        if (result.IsSuccess &&
            result.Data is not null)
        {
            model.Reviews =
                result.Data;
        }
        else
        {
            model.ErrorMessage =
                result.Message
                ?? "Không thể tải danh sách đánh giá.";
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
            await _reviewService.GetReviewByIdAsync(
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
            new AdminReviewDetailViewModel
            {
                ReturnUrl =
                    !string.IsNullOrWhiteSpace(
                        returnUrl)
                    &&
                    Url.IsLocalUrl(
                        returnUrl)

                        ? returnUrl

                        : Url.Action(
                            nameof(Index))
            };


        if (!result.IsSuccess ||
            result.Data is null)
        {
            model.ErrorMessage =
                result.Message
                ?? "Không thể tải thông tin đánh giá.";

            return View(model);
        }


        model.Review =
            result.Data;


        return View(model);
    }

    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Hide(
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
            await _reviewService.HideReviewAsync(
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
                ?? "Không thể ẩn đánh giá.";

            return RedirectToAction(
                nameof(Details),
                new
                {
                    id,
                    returnUrl
                });
        }


        TempData["SuccessMessage"] =
            "Đánh giá đã được ẩn khỏi người dùng.";

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
    public async Task<IActionResult> Show(
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
            await _reviewService.ShowReviewAsync(
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
                ?? "Không thể hiển thị lại đánh giá.";

            return RedirectToAction(
                nameof(Details),
                new
                {
                    id,
                    returnUrl
                });
        }


        TempData["SuccessMessage"] =
            "Đánh giá đã được hiển thị lại.";

        return RedirectToAction(
            nameof(Details),
            new
            {
                id,
                returnUrl
            });
    }
}