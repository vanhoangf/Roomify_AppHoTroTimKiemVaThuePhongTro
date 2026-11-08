using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Roomify.API.DTOs.Reviews;
using Roomify.API.Helpers;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Controllers;

[ApiController]
public class ReviewsController : ControllerBase
{
    private readonly IReviewService _reviewService;

    public ReviewsController(IReviewService reviewService)
    {
        _reviewService = reviewService;
    }

    // ============================================================
    // STUDENT - CREATE REVIEW
    // ============================================================

    // POST:
    // /api/student/contracts/{contractId}/review
    //
    // Sinh viên tạo đánh giá cho hợp đồng của chính mình.
    //
    // Điều kiện chính được xử lý ở ReviewService:
    // - Contract phải thuộc student hiện tại
    // - Contract phải đủ điều kiện để review
    // - Mỗi contract chỉ được review một lần
    // - Rating từ 1 đến 5
    [HttpPost("api/student/contracts/{contractId:long}/review")]
    [Authorize(Roles = "STUDENT")]
    public async Task<ActionResult<ReviewResponse>> CreateReview(
        long contractId,
        [FromBody] CreateReviewRequest request)
    {
        var userId = User.GetRequiredUserId();

        var result = await _reviewService.CreateAsync(
            userId,
            contractId,
            request);

        return StatusCode(
            StatusCodes.Status201Created,
            result);
    }

    // ============================================================
    // STUDENT - GET OWN REVIEW BY CONTRACT
    // ============================================================

    // GET:
    // /api/student/contracts/{contractId}/review
    //
    // Sinh viên xem review của chính mình theo ContractID.
    [HttpGet("api/student/contracts/{contractId:long}/review")]
    [Authorize(Roles = "STUDENT")]
    public async Task<ActionResult<ReviewResponse>> GetStudentReviewByContract(
        long contractId)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _reviewService.GetStudentReviewByContractAsync(
                userId,
                contractId);

        return Ok(result);
    }

    // ============================================================
    // PUBLIC - GET ROOM REVIEWS
    // ============================================================

    // GET:
    // /api/rooms/{roomId}/reviews?page=1&pageSize=10
    //
    // Public xem danh sách review VISIBLE của phòng.
    [HttpGet("api/rooms/{roomId:long}/reviews")]
    [AllowAnonymous]
    public async Task<ActionResult<ReviewListResponse>> GetRoomReviews(
        long roomId,
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 10)
    {
        var result = await _reviewService.GetRoomReviewsAsync(
            roomId,
            page,
            pageSize);

        return Ok(result);
    }
}