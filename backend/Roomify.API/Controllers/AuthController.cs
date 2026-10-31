using Microsoft.AspNetCore.Mvc;
using Roomify.API.DTOs.Auth;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Controllers;

[ApiController]
[Route("api/auth")]
public class AuthController : ControllerBase
{
    private readonly IAuthService _authService;

    public AuthController(
        IAuthService authService)
    {
        _authService = authService;
    }

    [HttpPost("register/student")]
    public async Task<IActionResult> RegisterStudent(
        [FromBody] RegisterStudentRequest request,
        CancellationToken cancellationToken)
    {
        var result =
            await _authService.RegisterStudentAsync(
                request,
                cancellationToken);

        return StatusCode(
            StatusCodes.Status201Created,
            result);
    }

    [HttpPost("register/landlord")]
    public async Task<IActionResult> RegisterLandlord(
        [FromBody] RegisterLandlordRequest request,
        CancellationToken cancellationToken)
    {
        var result =
            await _authService.RegisterLandlordAsync(
                request,
                cancellationToken);

        return StatusCode(
            StatusCodes.Status201Created,
            result);
    }
}