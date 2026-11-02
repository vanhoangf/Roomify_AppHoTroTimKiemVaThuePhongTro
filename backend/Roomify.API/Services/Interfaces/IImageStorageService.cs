using Microsoft.AspNetCore.Http;

namespace Roomify.API.Services.Interfaces;

public interface IImageStorageService
{
    Task<StoredImageResult> UploadAsync(
        IFormFile file,
        string folder,
        CancellationToken cancellationToken = default);

    Task DeleteAsync(
        string publicId,
        CancellationToken cancellationToken = default);
}

public record StoredImageResult(
    string Url,
    string PublicId);