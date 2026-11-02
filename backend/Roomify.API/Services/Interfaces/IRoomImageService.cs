using Microsoft.AspNetCore.Http;
using Roomify.API.DTOs.Landlord;

namespace Roomify.API.Services.Interfaces;

public interface IRoomImageService
{
    Task<IReadOnlyList<RoomImageResponse>>
        GetImagesAsync(
            long userId,
            long roomId,
            CancellationToken cancellationToken = default);

    Task<IReadOnlyList<RoomImageResponse>>
        UploadImagesAsync(
            long userId,
            long roomId,
            IReadOnlyList<IFormFile> files,
            CancellationToken cancellationToken = default);

    Task<RoomImageResponse>
        SetPrimaryAsync(
            long userId,
            long roomId,
            long imageId,
            CancellationToken cancellationToken = default);

    Task<IReadOnlyList<RoomImageResponse>>
        ReorderAsync(
            long userId,
            long roomId,
            ReorderRoomImagesRequest request,
            CancellationToken cancellationToken = default);

    Task DeleteAsync(
        long userId,
        long roomId,
        long imageId,
        CancellationToken cancellationToken = default);
}