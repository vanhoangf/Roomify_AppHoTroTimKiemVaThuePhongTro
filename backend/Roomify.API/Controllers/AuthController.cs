using Microsoft.AspNetCore.Mvc;
using Roomify.API.DTOs.Auth;
using Roomify.API.Services.Interfaces;
using System.IdentityModel.Tokens.Jwt;
using Microsoft.AspNetCore.Authorization;
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

    [HttpPost("login")]
    public async Task<IActionResult> Login(
    [FromBody] LoginRequest request,
    CancellationToken cancellationToken)
    {
        var result =
            await _authService.LoginAsync(
                request,
                cancellationToken);

        return Ok(result);
    }

    [Authorize]
    [HttpGet("me")]
    public IActionResult Me()
    {
        return Ok(new
        {
            userId =
                User.FindFirst(
                    JwtRegisteredClaimNames.Sub)?.Value,

            email =
                User.FindFirst(
                    JwtRegisteredClaimNames.Email)?.Value,

            fullName =
                User.FindFirst("name")?.Value,

            role =
                User.FindFirst("role")?.Value
        });
    }
}