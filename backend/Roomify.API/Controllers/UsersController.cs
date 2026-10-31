using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

using Roomify.API.DTOs.Users;
using Roomify.API.Helpers;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Controllers;

[ApiController]
[Route("api/users")]
[Authorize]
public class UsersController : ControllerBase
{
    private readonly IUserService _userService;

    public UsersController(
        IUserService userService)
    {
        _userService = userService;
    }

    // ======================================================
    // GET CURRENT USER
    // ======================================================

    [HttpGet("me")]
    public async Task<IActionResult> GetMe(
        CancellationToken cancellationToken)
    {
        var userId =
            User.GetRequiredUserId();

        var result =
            await _userService.GetMeAsync(
                userId,
                cancellationToken);

        return Ok(result);
    }

    // ======================================================
    // UPDATE COMMON PROFILE
    // ======================================================

    [HttpPut("me")]
    public async Task<IActionResult> UpdateMe(
        [FromBody] UpdateUserRequest request,
        CancellationToken cancellationToken)
    {
        var userId =
            User.GetRequiredUserId();

        var result =
            await _userService.UpdateMeAsync(
                userId,
                request,
                cancellationToken);

        return Ok(result);
    }

    // ======================================================
    // UPDATE STUDENT PROFILE
    // ======================================================

    [Authorize(Roles = "STUDENT")]
    [HttpPut("me/student-profile")]
    public async Task<IActionResult>
        UpdateStudentProfile(
            [FromBody]
            UpdateStudentProfileRequest request,
            CancellationToken cancellationToken)
    {
        var userId =
            User.GetRequiredUserId();

        var result =
            await _userService
                .UpdateStudentProfileAsync(
                    userId,
                    request,
                    cancellationToken);

        return Ok(result);
    }

    // ======================================================
    // UPDATE LANDLORD PROFILE
    // ======================================================

    [Authorize(Roles = "LANDLORD")]
    [HttpPut("me/landlord-profile")]
    public async Task<IActionResult>
        UpdateLandlordProfile(
            [FromBody]
            UpdateLandlordProfileRequest request,
            CancellationToken cancellationToken)
    {
        var userId =
            User.GetRequiredUserId();

        var result =
            await _userService
                .UpdateLandlordProfileAsync(
                    userId,
                    request,
                    cancellationToken);

        return Ok(result);
    }
}