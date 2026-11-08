using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Roomify.API.DTOs.Admin.Rooms;
using Roomify.API.Helpers;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Controllers;

[ApiController]
[Route("api/admin/rooms")]
[Authorize(Roles = "ADMIN")]
public class AdminRoomsController : ControllerBase
{
    private readonly IAdminRoomService _adminRoomService;

    public AdminRoomsController(
        IAdminRoomService adminRoomService)
    {
        _adminRoomService = adminRoomService;
    }

    // ============================================================
    // GET LIST
    // GET /api/admin/rooms
    // ============================================================

    [HttpGet]
    public async Task<ActionResult<AdminRoomListResponse>> GetRooms(
        [FromQuery] string? listingStatus = null,
        [FromQuery] string? search = null,
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 10)
    {
        var result =
            await _adminRoomService.GetRoomsAsync(
                listingStatus,
                search,
                page,
                pageSize);

        return Ok(result);
    }

    // ============================================================
    // GET DETAIL
    // GET /api/admin/rooms/{roomId}
    // ============================================================

    [HttpGet("{roomId:long}")]
    public async Task<ActionResult<AdminRoomResponse>> GetRoomById(
        long roomId)
    {
        var result =
            await _adminRoomService.GetRoomByIdAsync(
                roomId);

        return Ok(result);
    }

    // ============================================================
    // APPROVE
    // PATCH /api/admin/rooms/{roomId}/approve
    // ============================================================

    [HttpPatch("{roomId:long}/approve")]
    public async Task<ActionResult<AdminRoomResponse>> ApproveRoom(
        long roomId)
    {
        var adminUserId =
            User.GetRequiredUserId();

        var result =
            await _adminRoomService.ApproveAsync(
                adminUserId,
                roomId);

        return Ok(result);
    }

    // ============================================================
    // REJECT
    // PATCH /api/admin/rooms/{roomId}/reject
    // ============================================================

    [HttpPatch("{roomId:long}/reject")]
    public async Task<ActionResult<AdminRoomResponse>> RejectRoom(
        long roomId,
        [FromBody] RejectRoomRequest request)
    {
        var adminUserId =
            User.GetRequiredUserId();

        var result =
            await _adminRoomService.RejectAsync(
                adminUserId,
                roomId,
                request);

        return Ok(result);
    }
}