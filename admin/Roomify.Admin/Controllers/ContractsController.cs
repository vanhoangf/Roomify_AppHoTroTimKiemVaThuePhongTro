using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

using Roomify.Admin.Models.Contracts;
using Roomify.Admin.Services.Contracts;

namespace Roomify.Admin.Controllers;

[Authorize(Roles = "ADMIN")]
public class ContractsController : Controller
{
    private readonly IAdminContractApiService
        _contractService;


    public ContractsController(
        IAdminContractApiService contractService)
    {
        _contractService =
            contractService;
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


        var normalizedStatus =
            string.IsNullOrWhiteSpace(status)
                ? null
                : status.Trim()
                    .ToUpperInvariant();


        if (normalizedStatus is not null &&
            normalizedStatus is not "PENDING"
            and not "ACTIVE"
            and not "COMPLETED"
            and not "CANCELLED")
        {
            normalizedStatus =
                null;
        }


        var normalizedSearch =
            string.IsNullOrWhiteSpace(search)
                ? null
                : search.Trim();


        var result =
            await _contractService
                .GetContractsAsync(
                    normalizedStatus,
                    normalizedSearch,
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
            new AdminContractsViewModel
            {
                Search =
                    normalizedSearch,

                Status =
                    normalizedStatus,

                Page =
                    page,

                PageSize =
                    pageSize
            };


        if (result.IsSuccess &&
            result.Data is not null)
        {
            model.Contracts =
                result.Data;
        }
        else
        {
            model.ErrorMessage =
                result.Message
                ?? "Không thể tải danh sách hợp đồng.";
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
            await _contractService
                .GetContractByIdAsync(
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
            new AdminContractDetailViewModel
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
                ?? "Không thể tải thông tin hợp đồng.";

            return View(model);
        }


        model.Contract =
            result.Data;


        return View(model);
    }
}