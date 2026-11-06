using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Roomify.API.DTOs.Rentals;
using Roomify.API.Helpers;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Controllers;

[ApiController]
[Authorize(Roles = "STUDENT")]
[Route("api/student")]
public class StudentRentalRequestsController : ControllerBase
{
    private readonly IRentalRequestService _rentalRequestService;

    public StudentRentalRequestsController(
        IRentalRequestService rentalRequestService)
    {
        _rentalRequestService = rentalRequestService;
    }

    [HttpPost("rooms/{roomId:long}/rental-requests")]
    public async Task<ActionResult<RentalRequestResponse>> Create(
        long roomId,
        [FromBody] CreateRentalRequestRequest request)
    {
        var userId = User.GetRequiredUserId();

        var result = await _rentalRequestService.CreateAsync(
            userId,
            roomId,
            request);

        return StatusCode(StatusCodes.Status201Created, result);
    }

    [HttpGet("rental-requests")]
    public async Task<ActionResult<RentalRequestListResponse>> GetList(
        [FromQuery] string? status = null,
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 10)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _rentalRequestService.GetStudentListAsync(
                userId,
                status,
                page,
                pageSize);

        return Ok(result);
    }

    [HttpGet("rental-requests/{rentalRequestId:long}")]
    public async Task<ActionResult<RentalRequestResponse>> GetDetail(
        long rentalRequestId)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _rentalRequestService.GetStudentDetailAsync(
                userId,
                rentalRequestId);

        return Ok(result);
    }

    [HttpPatch("rental-requests/{rentalRequestId:long}/cancel")]
    public async Task<ActionResult<RentalRequestResponse>> Cancel(
        long rentalRequestId)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _rentalRequestService.CancelByStudentAsync(
                userId,
                rentalRequestId);

        return Ok(result);
    }
}