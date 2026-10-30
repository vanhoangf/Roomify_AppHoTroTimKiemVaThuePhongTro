using Microsoft.AspNetCore.Mvc;
using Roomify.API.Data;

namespace Roomify.API.Controllers;

[ApiController]
[Route("api/[controller]")]
public class HealthController : ControllerBase
{
    private readonly RoomifyDbContext _dbContext;

    public HealthController(RoomifyDbContext dbContext)
    {
        _dbContext = dbContext;
    }

    [HttpGet]
    public async Task<IActionResult> GetHealth(
        CancellationToken cancellationToken)
    {
        var canConnect = await _dbContext.Database
            .CanConnectAsync(cancellationToken);

        if (!canConnect)
        {
            return StatusCode(
                StatusCodes.Status503ServiceUnavailable,
                new
                {
                    status = "ERROR",
                    database = "DISCONNECTED",
                    serverTime = DateTime.UtcNow
                });
        }

        return Ok(new
        {
            status = "OK",
            database = "CONNECTED",
            serverTime = DateTime.UtcNow
        });
    }
}