
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

using Roomify.API.Helpers;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Controllers;

[ApiController]
[Route("api/student/saved-rooms")]
[Authorize(Roles = "STUDENT")]
public class StudentSavedRoomsController : ControllerBase
{
    private readonly ISavedRoomService _savedRoomService;

    public StudentSavedRoomsController(
        ISavedRoomService savedRoomService)
    {
        _savedRoomService = savedRoomService;
    }

    // GET /api/student/saved-rooms
    [HttpGet]
    public async Task<IActionResult> GetSavedRooms(
        CancellationToken cancellationToken)
    {
        var userId = User.GetRequiredUserId();

        var result = await _savedRoomService
            .GetSavedRoomsAsync(
                userId,
                cancellationToken);

        return Ok(result);
    }

    // POST /api/student/saved-rooms/{roomId}
    [HttpPost("{roomId:long}")]
    public async Task<IActionResult> SaveRoom(
        long roomId,
        CancellationToken cancellationToken)
    {
        var userId = User.GetRequiredUserId();

        var result = await _savedRoomService
            .SaveRoomAsync(
                userId,
                roomId,
                cancellationToken);

        return StatusCode(
            StatusCodes.Status201Created,
            result);
    }

    // DELETE /api/student/saved-rooms/{roomId}
    [HttpDelete("{roomId:long}")]
    public async Task<IActionResult> DeleteSavedRoom(
        long roomId,
        CancellationToken cancellationToken)
    {
        var userId = User.GetRequiredUserId();

        await _savedRoomService.DeleteSavedRoomAsync(
            userId,
            roomId,
            cancellationToken);

        return NoContent();
    }
}
