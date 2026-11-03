
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

using Roomify.API.DTOs.Public;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Controllers;

[ApiController]
[Route("api/rooms")]
[AllowAnonymous]
public class PublicRoomsController : ControllerBase
{
    private readonly IRoomDiscoveryService _roomDiscoveryService;

    public PublicRoomsController(
        IRoomDiscoveryService roomDiscoveryService)
    {
        _roomDiscoveryService = roomDiscoveryService;
    }

    // GET /api/rooms
    [HttpGet]
    public async Task<IActionResult> SearchRooms(
        [FromQuery] SearchRoomsRequest request,
        CancellationToken cancellationToken)
    {
        var result = await _roomDiscoveryService
            .SearchRoomsAsync(
                request,
                cancellationToken);

        return Ok(result);
    }

    // GET /api/rooms/{roomId}
    [HttpGet("{roomId:long}")]
    public async Task<IActionResult> GetRoomDetail(
        long roomId,
        CancellationToken cancellationToken)
    {
        var result = await _roomDiscoveryService
            .GetRoomDetailAsync(
                roomId,
                cancellationToken);

        return Ok(result);
    }
}
