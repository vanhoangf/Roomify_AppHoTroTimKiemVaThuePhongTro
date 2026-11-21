using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

using Roomify.Admin.Models.Statistics;
using Roomify.Admin.Services.Statistics;

namespace Roomify.Admin.Controllers;

[Authorize(Roles = "ADMIN")]
public class StatisticsController : Controller
{
    private readonly IAdminStatisticsApiService
        _statisticsService;


    public StatisticsController(
        IAdminStatisticsApiService statisticsService)
    {
        _statisticsService =
            statisticsService;
    }


    [HttpGet]
    public async Task<IActionResult> Index(
        string? range = "30",
        DateOnly? fromDate = null,
        DateOnly? toDate = null,
        CancellationToken cancellationToken = default)
    {
        var normalizedRange =
            string.IsNullOrWhiteSpace(range)
                ? "30"
                : range.Trim()
                    .ToLowerInvariant();


        if (normalizedRange is not "7"
            and not "30"
            and not "90"
            and not "year"
            and not "custom")
        {
            normalizedRange =
                "30";
        }


        var model =
            new AdminStatisticsViewModel
            {
                Range =
                    normalizedRange,

                CustomFromDate =
                    fromDate,

                CustomToDate =
                    toDate
            };


        // Đồng bộ với backend:
        // thống kê theo ngày UTC.
        var today =
            DateOnly.FromDateTime(
                DateTime.UtcNow);


        DateOnly? resolvedFrom =
            null;

        DateOnly? resolvedTo =
            null;


        switch (normalizedRange)
        {
            case "7":

                resolvedFrom =
                    today.AddDays(-6);

                resolvedTo =
                    today;

                break;


            case "30":

                resolvedFrom =
                    today.AddDays(-29);

                resolvedTo =
                    today;

                break;


            case "90":

                resolvedFrom =
                    today.AddDays(-89);

                resolvedTo =
                    today;

                break;


            case "year":

                resolvedFrom =
                    new DateOnly(
                        today.Year,
                        1,
                        1);

                resolvedTo =
                    today;

                break;


            case "custom":

                if (!fromDate.HasValue ||
                    !toDate.HasValue)
                {
                    model.ErrorMessage =
                        "Vui lòng chọn đầy đủ từ ngày và đến ngày.";

                    return View(
                        model);
                }


                resolvedFrom =
                    fromDate.Value;

                resolvedTo =
                    toDate.Value;

                break;
        }


        var result =
            await _statisticsService
                .GetStatisticsAsync(
                    resolvedFrom,
                    resolvedTo,
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


        if (result.IsSuccess &&
            result.Data is not null)
        {
            model.Statistics =
                result.Data;
        }
        else
        {
            model.ErrorMessage =
                result.Message
                ?? "Không thể tải dữ liệu thống kê.";
        }


        return View(
            model);
    }
}