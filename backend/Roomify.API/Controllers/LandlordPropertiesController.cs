using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

using Roomify.API.DTOs.Landlord;
using Roomify.API.Helpers;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Controllers;

[ApiController]
[Route("api/landlord")]
[Authorize(Roles = "LANDLORD")]
public class LandlordPropertiesController : ControllerBase
{
    private readonly ILandlordPropertyService _service;

    public LandlordPropertiesController(
        ILandlordPropertyService service)
    {
        _service = service;
    }

    [HttpGet("properties")]
    public async Task<IActionResult> GetProperties(
        CancellationToken cancellationToken)
    {
        var userId =
            User.GetRequiredUserId();

        return Ok(
            await _service.GetPropertiesAsync(
                userId,
                cancellationToken));
    }

    [HttpGet("properties/{propertyId:long}")]
    public async Task<IActionResult> GetProperty(
        long propertyId,
        CancellationToken cancellationToken)
    {
        var userId =
            User.GetRequiredUserId();

        return Ok(
            await _service.GetPropertyAsync(
                userId,
                propertyId,
                cancellationToken));
    }

    [HttpPost("properties")]
    public async Task<IActionResult> CreateProperty(
        [FromBody] CreatePropertyRequest request,
        CancellationToken cancellationToken)
    {
        var userId =
            User.GetRequiredUserId();

        var result =
            await _service.CreatePropertyAsync(
                userId,
                request,
                cancellationToken);

        return CreatedAtAction(
            nameof(GetProperty),
            new
            {
                propertyId = result.PropertyId
            },
            result);
    }

    [HttpPut("properties/{propertyId:long}")]
    public async Task<IActionResult> UpdateProperty(
        long propertyId,
        [FromBody] UpdatePropertyRequest request,
        CancellationToken cancellationToken)
    {
        var userId =
            User.GetRequiredUserId();

        return Ok(
            await _service.UpdatePropertyAsync(
                userId,
                propertyId,
                request,
                cancellationToken));
    }

    [HttpPatch("properties/{propertyId:long}/status")]
    public async Task<IActionResult> UpdatePropertyStatus(
        long propertyId,
        [FromBody] UpdatePropertyStatusRequest request,
        CancellationToken cancellationToken)
    {
        var userId =
            User.GetRequiredUserId();

        return Ok(
            await _service.UpdatePropertyStatusAsync(
                userId,
                propertyId,
                request,
                cancellationToken));
    }

    [HttpGet("properties/{propertyId:long}/rooms")]
    public async Task<IActionResult> GetRooms(
        long propertyId,
        CancellationToken cancellationToken)
    {
        var userId =
            User.GetRequiredUserId();

        return Ok(
            await _service.GetRoomsAsync(
                userId,
                propertyId,
                cancellationToken));
    }

    [HttpPost("properties/{propertyId:long}/rooms")]
    public async Task<IActionResult> CreateRoom(
        long propertyId,
        [FromBody] CreateRoomRequest request,
        CancellationToken cancellationToken)
    {
        var userId =
            User.GetRequiredUserId();

        var result =
            await _service.CreateRoomAsync(
                userId,
                propertyId,
                request,
                cancellationToken);

        return Created(
            $"/api/landlord/rooms/{result.RoomId}",
            result);
    }

    [HttpGet("rooms/{roomId:long}")]
    public async Task<IActionResult> GetRoom(
        long roomId,
        CancellationToken cancellationToken)
    {
        var userId =
            User.GetRequiredUserId();

        return Ok(
            await _service.GetRoomAsync(
                userId,
                roomId,
                cancellationToken));
    }

    [HttpPut("rooms/{roomId:long}")]
    public async Task<IActionResult> UpdateRoom(
        long roomId,
        [FromBody] UpdateRoomRequest request,
        CancellationToken cancellationToken)
    {
        var userId =
            User.GetRequiredUserId();

        return Ok(
            await _service.UpdateRoomAsync(
                userId,
                roomId,
                request,
                cancellationToken));
    }

    [HttpPut("rooms/{roomId:long}/amenities")]
    public async Task<IActionResult> UpdateAmenities(
        long roomId,
        [FromBody] UpdateRoomAmenitiesRequest request,
        CancellationToken cancellationToken)
    {
        var userId =
            User.GetRequiredUserId();

        return Ok(
            await _service.UpdateAmenitiesAsync(
                userId,
                roomId,
                request,
                cancellationToken));
    }

    [HttpPatch("rooms/{roomId:long}/availability")]
    public async Task<IActionResult> UpdateAvailability(
        long roomId,
        [FromBody] UpdateRoomAvailabilityRequest request,
        CancellationToken cancellationToken)
    {
        var userId =
            User.GetRequiredUserId();

        return Ok(
            await _service.UpdateAvailabilityAsync(
                userId,
                roomId,
                request,
                cancellationToken));
    }

    [HttpPost("rooms/{roomId:long}/submit-review")]
    public async Task<IActionResult> SubmitReview(
        long roomId,
        CancellationToken cancellationToken)
    {
        var userId =
            User.GetRequiredUserId();

        return Ok(
            await _service.SubmitForReviewAsync(
                userId,
                roomId,
                cancellationToken));
    }

    [HttpPost("rooms/{roomId:long}/hide")]
    public async Task<IActionResult> HideRoom(
        long roomId,
        CancellationToken cancellationToken)
    {
        var userId =
            User.GetRequiredUserId();

        return Ok(
            await _service.HideRoomAsync(
                userId,
                roomId,
                cancellationToken));
    }
}