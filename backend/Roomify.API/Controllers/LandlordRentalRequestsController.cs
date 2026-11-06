using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Roomify.API.DTOs.Rentals;
using Roomify.API.Helpers;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Controllers;

[ApiController]
[Authorize(Roles = "LANDLORD")]
[Route("api/landlord/rental-requests")]
public class LandlordRentalRequestsController : ControllerBase
{
    private readonly IRentalRequestService _rentalRequestService;

    public LandlordRentalRequestsController(
        IRentalRequestService rentalRequestService)
    {
        _rentalRequestService = rentalRequestService;
    }

    [HttpGet]
    public async Task<ActionResult<RentalRequestListResponse>> GetList(
        [FromQuery] string? status = null,
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 10)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _rentalRequestService.GetLandlordListAsync(
                userId,
                status,
                page,
                pageSize);

        return Ok(result);
    }

    [HttpGet("{rentalRequestId:long}")]
    public async Task<ActionResult<RentalRequestResponse>> GetDetail(
        long rentalRequestId)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _rentalRequestService.GetLandlordDetailAsync(
                userId,
                rentalRequestId);

        return Ok(result);
    }

    [HttpPatch("{rentalRequestId:long}/approve")]
    public async Task<ActionResult<RentalRequestResponse>> Approve(
        long rentalRequestId,
        [FromBody] RentalRequestDecisionRequest request)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _rentalRequestService.ApproveAsync(
                userId,
                rentalRequestId,
                request);

        return Ok(result);
    }

    [HttpPatch("{rentalRequestId:long}/reject")]
    public async Task<ActionResult<RentalRequestResponse>> Reject(
        long rentalRequestId,
        [FromBody] RentalRequestDecisionRequest request)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _rentalRequestService.RejectAsync(
                userId,
                rentalRequestId,
                request);

        return Ok(result);
    }
}