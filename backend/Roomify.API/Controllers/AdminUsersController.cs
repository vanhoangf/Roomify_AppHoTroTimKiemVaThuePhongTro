using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Roomify.API.DTOs.Admin.Users;
using Roomify.API.Helpers;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Controllers;

[ApiController]
[Route("api/admin")]
[Authorize(Roles = "ADMIN")]
public class AdminUsersController : ControllerBase
{
    private readonly IAdminUserService _adminUserService;

    public AdminUsersController(
        IAdminUserService adminUserService)
    {
        _adminUserService = adminUserService;
    }

    // ============================================================
    // USERS - LIST
    // GET /api/admin/users
    // ============================================================

    [HttpGet("users")]
    public async Task<ActionResult<AdminUserListResponse>> GetUsers(
        [FromQuery] string? role = null,
        [FromQuery] string? status = null,
        [FromQuery] bool? verified = null,
        [FromQuery] string? search = null,
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 10)
    {
        var result = await _adminUserService.GetUsersAsync(
            role,
            status,
            verified,
            search,
            page,
            pageSize);

        return Ok(result);
    }

    // ============================================================
    // USERS - DETAIL
    // GET /api/admin/users/{userId}
    // ============================================================

    [HttpGet("users/{userId:long}")]
    public async Task<ActionResult<AdminUserResponse>> GetUserById(
        long userId)
    {
        var result =
            await _adminUserService.GetUserByIdAsync(
                userId);

        return Ok(result);
    }

    // ============================================================
    // LOCK USER
    // PATCH /api/admin/users/{userId}/lock
    // ============================================================

    [HttpPatch("users/{userId:long}/lock")]
    public async Task<ActionResult<AdminUserResponse>> LockUser(
        long userId,
        [FromBody] LockUserRequest request)
    {
        var adminUserId =
            User.GetRequiredUserId();

        var result =
            await _adminUserService.LockUserAsync(
                adminUserId,
                userId,
                request);

        return Ok(result);
    }

    // ============================================================
    // UNLOCK USER
    // PATCH /api/admin/users/{userId}/unlock
    // ============================================================

    [HttpPatch("users/{userId:long}/unlock")]
    public async Task<ActionResult<AdminUserResponse>> UnlockUser(
        long userId)
    {
        var adminUserId =
            User.GetRequiredUserId();

        var result =
            await _adminUserService.UnlockUserAsync(
                adminUserId,
                userId);

        return Ok(result);
    }

    // ============================================================
    // VERIFY STUDENT
    // PATCH /api/admin/students/{studentId}/verify
    // ============================================================

    [HttpPatch("students/{studentId:long}/verify")]
    public async Task<ActionResult<AdminUserResponse>> VerifyStudent(
        long studentId)
    {
        var adminUserId =
            User.GetRequiredUserId();

        var result =
            await _adminUserService.VerifyStudentAsync(
                adminUserId,
                studentId);

        return Ok(result);
    }

    // ============================================================
    // VERIFY LANDLORD
    // PATCH /api/admin/landlords/{landlordId}/verify
    // ============================================================

    [HttpPatch("landlords/{landlordId:long}/verify")]
    public async Task<ActionResult<AdminUserResponse>> VerifyLandlord(
        long landlordId)
    {
        var adminUserId =
            User.GetRequiredUserId();

        var result =
            await _adminUserService.VerifyLandlordAsync(
                adminUserId,
                landlordId);

        return Ok(result);
    }
}