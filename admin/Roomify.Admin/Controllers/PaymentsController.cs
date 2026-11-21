using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

using Roomify.Admin.Models.Payments;
using Roomify.Admin.Services.Payments;

namespace Roomify.Admin.Controllers;

[Authorize(Roles = "ADMIN")]
public class PaymentsController : Controller
{
    private readonly IAdminPaymentApiService
        _paymentService;


    public PaymentsController(
        IAdminPaymentApiService paymentService)
    {
        _paymentService =
            paymentService;
    }


    [HttpGet]
    public async Task<IActionResult> Index(
        string? search = null,
        string? status = null,
        string? paymentType = null,
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


        // ========================================================
        // STATUS
        // ========================================================

        var normalizedStatus =
            string.IsNullOrWhiteSpace(status)
                ? null
                : status.Trim()
                    .ToUpperInvariant();


        if (normalizedStatus is not null &&
            normalizedStatus is not "PENDING"
            and not "PAID"
            and not "FAILED"
            and not "CANCELLED")
        {
            normalizedStatus =
                null;
        }


        // ========================================================
        // PAYMENT TYPE
        // ========================================================

        var normalizedPaymentType =
            string.IsNullOrWhiteSpace(paymentType)
                ? null
                : paymentType.Trim()
                    .ToUpperInvariant();


        if (normalizedPaymentType is not null &&
            normalizedPaymentType is not "DEPOSIT"
            and not "RENT"
            and not "UTILITY"
            and not "OTHER")
        {
            normalizedPaymentType =
                null;
        }


        // ========================================================
        // SEARCH
        // ========================================================

        var normalizedSearch =
            string.IsNullOrWhiteSpace(search)
                ? null
                : search.Trim();


        var result =
            await _paymentService
                .GetPaymentsAsync(
                    normalizedStatus,
                    normalizedPaymentType,
                    normalizedSearch,
                    page,
                    pageSize,
                    cancellationToken);


        // ========================================================
        // SESSION EXPIRED
        // ========================================================

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


        // ========================================================
        // MODEL
        // ========================================================

        var model =
            new AdminPaymentsViewModel
            {
                Search =
                    normalizedSearch,

                Status =
                    normalizedStatus,

                PaymentType =
                    normalizedPaymentType,

                Page =
                    page,

                PageSize =
                    pageSize
            };


        if (result.IsSuccess &&
            result.Data is not null)
        {
            model.Payments =
                result.Data;
        }
        else
        {
            model.ErrorMessage =
                result.Message
                ?? "Không thể tải danh sách thanh toán.";
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
            await _paymentService
                .GetPaymentByIdAsync(
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
            new AdminPaymentDetailViewModel
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
                ?? "Không thể tải thông tin thanh toán.";

            return View(model);
        }


        model.Payment =
            result.Data;


        return View(model);
    }
}