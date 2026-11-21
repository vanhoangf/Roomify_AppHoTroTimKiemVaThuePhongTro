using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

using Roomify.API.DTOs.Admin.Reviews;
using Roomify.API.Helpers;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Controllers;

[ApiController]
[Route("api/admin/reviews")]
[Authorize(Roles = "ADMIN")]
public class AdminReviewsController
    : ControllerBase
{
    private readonly IAdminReviewService
        _adminReviewService;


    public AdminReviewsController(
        IAdminReviewService adminReviewService)
    {
        _adminReviewService =
            adminReviewService;
    }


    // ============================================================
    // GET LIST
    // ============================================================

    // GET:
    // /api/admin/reviews
    // ?status=VISIBLE
    // &rating=5
    // &search=...
    // &page=1
    // &pageSize=10

    [HttpGet]
    public async Task<ActionResult<AdminReviewListResponse>>
        GetReviews(
            [FromQuery] string? status = null,
            [FromQuery] int? rating = null,
            [FromQuery] string? search = null,
            [FromQuery] int page = 1,
            [FromQuery] int pageSize = 10)
    {
        var result =
            await _adminReviewService
                .GetReviewsAsync(
                    status,
                    rating,
                    search,
                    page,
                    pageSize);


        return Ok(result);
    }


    // ============================================================
    // GET DETAIL
    // ============================================================

    // GET /api/admin/reviews/{reviewId}

    [HttpGet("{reviewId:long}")]
    public async Task<ActionResult<AdminReviewResponse>>
        GetReviewById(
            long reviewId)
    {
        var result =
            await _adminReviewService
                .GetReviewByIdAsync(
                    reviewId);


        return Ok(result);
    }


    // ============================================================
    // HIDE
    // ============================================================

    // PATCH /api/admin/reviews/{reviewId}/hide

    [HttpPatch("{reviewId:long}/hide")]
    public async Task<ActionResult<AdminReviewResponse>>
        HideReview(
            long reviewId)
    {
        var adminUserId =
            User.GetRequiredUserId();


        var result =
            await _adminReviewService
                .HideAsync(
                    adminUserId,
                    reviewId);


        return Ok(result);
    }


    // ============================================================
    // SHOW
    // ============================================================

    // PATCH /api/admin/reviews/{reviewId}/show

    [HttpPatch("{reviewId:long}/show")]
    public async Task<ActionResult<AdminReviewResponse>>
        ShowReview(
            long reviewId)
    {
        var adminUserId =
            User.GetRequiredUserId();


        var result =
            await _adminReviewService
                .ShowAsync(
                    adminUserId,
                    reviewId);


        return Ok(result);
    }
}