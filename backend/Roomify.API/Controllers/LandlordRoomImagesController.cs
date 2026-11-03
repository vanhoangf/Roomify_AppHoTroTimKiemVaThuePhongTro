using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

using Roomify.API.DTOs.Landlord;
using Roomify.API.Helpers;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Controllers;

[ApiController]
[Route(
    "api/landlord/rooms/{roomId:long}/images")]
[Authorize(Roles = "LANDLORD")]
public class LandlordRoomImagesController
    : ControllerBase
{
    private readonly IRoomImageService
        _roomImageService;

    public LandlordRoomImagesController(
        IRoomImageService roomImageService)
    {
        _roomImageService =
            roomImageService;
    }

    [HttpGet]
    public async Task<IActionResult> GetImages(
        long roomId,
        CancellationToken cancellationToken)
    {
        var userId =
            User.GetRequiredUserId();

        return Ok(
            await _roomImageService
                .GetImagesAsync(
                    userId,
                    roomId,
                    cancellationToken));
    }

    [HttpPost]
    [Consumes("multipart/form-data")]
    public async Task<IActionResult> UploadImages(
        long roomId,
        [FromForm] List<IFormFile> files,
        CancellationToken cancellationToken)
    {
        var userId =
            User.GetRequiredUserId();

        var result =
            await _roomImageService
                .UploadImagesAsync(
                    userId,
                    roomId,
                    files,
                    cancellationToken);

        return Ok(result);
    }

    [HttpPatch("{imageId:long}/primary")]
    public async Task<IActionResult> SetPrimary(
        long roomId,
        long imageId,
        CancellationToken cancellationToken)
    {
        var userId =
            User.GetRequiredUserId();

        return Ok(
            await _roomImageService
                .SetPrimaryAsync(
                    userId,
                    roomId,
                    imageId,
                    cancellationToken));
    }

    [HttpPut("order")]
    public async Task<IActionResult> Reorder(
        long roomId,
        [FromBody]
        ReorderRoomImagesRequest request,
        CancellationToken cancellationToken)
    {
        var userId =
            User.GetRequiredUserId();

        return Ok(
            await _roomImageService
                .ReorderAsync(
                    userId,
                    roomId,
                    request,
                    cancellationToken));
    }

    [HttpDelete("{imageId:long}")]
    public async Task<IActionResult> DeleteImage(
        long roomId,
        long imageId,
        CancellationToken cancellationToken)
    {
        var userId =
            User.GetRequiredUserId();

        await _roomImageService.DeleteAsync(
            userId,
            roomId,
            imageId,
            cancellationToken);

        return NoContent();
    }
}