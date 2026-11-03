using CloudinaryDotNet;
using CloudinaryDotNet.Actions;

using Roomify.API.Services.Interfaces;

namespace Roomify.API.Services.Implementations;

public class CloudinaryImageStorageService
    : IImageStorageService
{
    private readonly Cloudinary _cloudinary;

    public CloudinaryImageStorageService(
        Cloudinary cloudinary)
    {
        _cloudinary = cloudinary;
    }

    public async Task<StoredImageResult> UploadAsync(
        IFormFile file,
        string folder,
        CancellationToken cancellationToken = default)
    {
        cancellationToken.ThrowIfCancellationRequested();

        await using var stream =
            file.OpenReadStream();

        var uploadParams =
            new ImageUploadParams
            {
                File =
                    new FileDescription(
                        file.FileName,
                        stream),

                Folder =
                    folder,

                UseFilename =
                    false,

                UniqueFilename =
                    true,

                Overwrite =
                    false
            };

        var result =
            await _cloudinary.UploadAsync(
                uploadParams);

        if (result.Error is not null)
        {
            throw new InvalidOperationException(
                $"Cloudinary upload failed: " +
                result.Error.Message);
        }

        if (result.SecureUrl is null ||
            string.IsNullOrWhiteSpace(
                result.PublicId))
        {
            throw new InvalidOperationException(
                "Cloudinary did not return a valid image URL.");
        }

        return new StoredImageResult(
            result.SecureUrl.ToString(),
            result.PublicId);
    }

    public async Task DeleteAsync(
        string publicId,
        CancellationToken cancellationToken = default)
    {
        cancellationToken.ThrowIfCancellationRequested();

        var deleteParams =
            new DeletionParams(publicId)
            {
                ResourceType =
                    ResourceType.Image,

                Invalidate =
                    true
            };

        var result =
            await _cloudinary.DestroyAsync(
                deleteParams);

        if (result.Result != "ok" &&
            result.Result != "not found")
        {
            throw new InvalidOperationException(
                "Không thể xóa ảnh khỏi Cloudinary.");
        }
    }
}