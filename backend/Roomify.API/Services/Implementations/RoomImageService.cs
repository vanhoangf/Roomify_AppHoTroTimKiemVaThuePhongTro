using Microsoft.AspNetCore.Http;
using Microsoft.EntityFrameworkCore;

using Roomify.API.Data;
using Roomify.API.DTOs.Landlord;
using Roomify.API.Entities;
using Roomify.API.Exceptions;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Services.Implementations;

public class RoomImageService : IRoomImageService
{
    private const long MaxImageSize =
        10 * 1024 * 1024;

    private static readonly HashSet<string>
        AllowedContentTypes =
        [
            "image/jpeg",
            "image/png",
            "image/webp"
        ];

    private readonly RoomifyDbContext _dbContext;
    private readonly IImageStorageService _imageStorage;

    public RoomImageService(
        RoomifyDbContext dbContext,
        IImageStorageService imageStorage)
    {
        _dbContext = dbContext;
        _imageStorage = imageStorage;
    }

    public async Task<
        IReadOnlyList<RoomImageResponse>>
        GetImagesAsync(
            long userId,
            long roomId,
            CancellationToken cancellationToken = default)
    {
        await GetOwnedRoomAsync(
            userId,
            roomId,
            false,
            cancellationToken);

        return await QueryImages(roomId)
            .ToListAsync(cancellationToken);
    }

    public async Task<
        IReadOnlyList<RoomImageResponse>>
        UploadImagesAsync(
            long userId,
            long roomId,
            IReadOnlyList<IFormFile> files,
            CancellationToken cancellationToken = default)
    {
        var room =
            await GetOwnedRoomAsync(
                userId,
                roomId,
                true,
                cancellationToken);

        if (files.Count == 0)
        {
            throw new BadRequestException(
                "IMAGE_REQUIRED",
                "Vui lòng chọn ít nhất một ảnh.");
        }

        foreach (var file in files)
        {
            ValidateFile(file);
        }

        var existingImages =
            await _dbContext.RoomImages
                .Where(x => x.RoomId == roomId)
                .ToListAsync(cancellationToken);

        var hasPrimary =
            existingImages.Any(x => x.IsPrimary);

        var nextSortOrder =
            existingImages.Count == 0
                ? 0
                : existingImages.Max(
                    x => x.SortOrder) + 1;

        var uploadedPublicIds =
            new List<string>();

        try
        {
            for (var i = 0; i < files.Count; i++)
            {
                var uploaded =
                    await _imageStorage.UploadAsync(
                        files[i],
                        $"roomify/rooms/{roomId}",
                        cancellationToken);

                uploadedPublicIds.Add(
                    uploaded.PublicId);

                var image =
                    new RoomImage
                    {
                        RoomId =
                            roomId,

                        ImageUrl =
                            uploaded.Url,

                        PublicId =
                            uploaded.PublicId,

                        IsPrimary =
                            !hasPrimary && i == 0,

                        SortOrder =
                            nextSortOrder++,

                        CreatedAt =
                            DateTime.UtcNow
                    };

                _dbContext.RoomImages.Add(image);
            }

            if (room.ListingStatus == "REJECTED")
            {
                ResetRejectedRoom(room);
            }

            room.UpdatedAt =
                DateTime.UtcNow;

            await _dbContext.SaveChangesAsync(
                cancellationToken);
        }
        catch
        {
            foreach (var publicId
                     in uploadedPublicIds)
            {
                try
                {
                    await _imageStorage.DeleteAsync(
                        publicId,
                        CancellationToken.None);
                }
                catch
                {
                    // Không che mất exception gốc.
                }
            }

            throw;
        }

        return await QueryImages(roomId)
            .ToListAsync(cancellationToken);
    }

    public async Task<RoomImageResponse>
        SetPrimaryAsync(
            long userId,
            long roomId,
            long imageId,
            CancellationToken cancellationToken = default)
    {
        var room =
            await GetOwnedRoomAsync(
                userId,
                roomId,
                true,
                cancellationToken);

        var images =
            await _dbContext.RoomImages
                .Where(x => x.RoomId == roomId)
                .ToListAsync(cancellationToken);

        var selected =
            images.SingleOrDefault(
                x => x.ImageId == imageId);

        if (selected is null)
        {
            throw new NotFoundException(
                "ROOM_IMAGE_NOT_FOUND",
                "Không tìm thấy ảnh phòng.");
        }

        await using var transaction =
            await _dbContext.Database
                .BeginTransactionAsync(
                    cancellationToken);

        foreach (var image in images)
        {
            image.IsPrimary = false;
        }

        await _dbContext.SaveChangesAsync(
            cancellationToken);

        selected.IsPrimary = true;

        room.UpdatedAt = DateTime.UtcNow;

        if (room.ListingStatus == "REJECTED")
        {
            ResetRejectedRoom(room);
        }

        await _dbContext.SaveChangesAsync(
            cancellationToken);

        await transaction.CommitAsync(
            cancellationToken);

        return await QueryImages(roomId)
            .SingleAsync(
                x => x.ImageId == imageId,
                cancellationToken);
    }

    public async Task<
        IReadOnlyList<RoomImageResponse>>
        ReorderAsync(
            long userId,
            long roomId,
            ReorderRoomImagesRequest request,
            CancellationToken cancellationToken = default)
    {
        var room =
            await GetOwnedRoomAsync(
                userId,
                roomId,
                true,
                cancellationToken);

        if (request.Images.Any(
            x => x.SortOrder < 0))
        {
            throw new BadRequestException(
                "INVALID_SORT_ORDER",
                "SortOrder không được nhỏ hơn 0.");
        }

        var duplicateImageIds =
            request.Images
                .GroupBy(x => x.ImageId)
                .Any(x => x.Count() > 1);

        if (duplicateImageIds)
        {
            throw new BadRequestException(
                "DUPLICATE_IMAGE_ID",
                "Danh sách ảnh chứa ImageID trùng.");
        }

        var images =
            await _dbContext.RoomImages
                .Where(x => x.RoomId == roomId)
                .ToListAsync(cancellationToken);

        foreach (var item in request.Images)
        {
            var image =
                images.SingleOrDefault(
                    x =>
                        x.ImageId ==
                        item.ImageId);

            if (image is null)
            {
                throw new NotFoundException(
                    "ROOM_IMAGE_NOT_FOUND",
                    "Một hoặc nhiều ảnh không tồn tại trong phòng.");
            }

            image.SortOrder =
                item.SortOrder;
        }

        room.UpdatedAt =
            DateTime.UtcNow;

        if (room.ListingStatus == "REJECTED")
        {
            ResetRejectedRoom(room);
        }

        await _dbContext.SaveChangesAsync(
            cancellationToken);

        return await QueryImages(roomId)
            .ToListAsync(cancellationToken);
    }

    public async Task DeleteAsync(
        long userId,
        long roomId,
        long imageId,
        CancellationToken cancellationToken = default)
    {
        var room =
            await GetOwnedRoomAsync(
                userId,
                roomId,
                true,
                cancellationToken);

        var image =
            await _dbContext.RoomImages
                .SingleOrDefaultAsync(
                    x =>
                        x.RoomId == roomId &&
                        x.ImageId == imageId,
                    cancellationToken);

        if (image is null)
        {
            throw new NotFoundException(
                "ROOM_IMAGE_NOT_FOUND",
                "Không tìm thấy ảnh phòng.");
        }

        var wasPrimary =
            image.IsPrimary;

        if (!string.IsNullOrWhiteSpace(
            image.PublicId))
        {
            await _imageStorage.DeleteAsync(
                image.PublicId,
                cancellationToken);
        }

        _dbContext.RoomImages.Remove(image);

        await _dbContext.SaveChangesAsync(
            cancellationToken);

        if (wasPrimary)
        {
            var nextImage =
                await _dbContext.RoomImages
                    .Where(
                        x =>
                            x.RoomId == roomId)
                    .OrderBy(x => x.SortOrder)
                    .ThenBy(x => x.ImageId)
                    .FirstOrDefaultAsync(
                        cancellationToken);

            if (nextImage is not null)
            {
                nextImage.IsPrimary =
                    true;
            }
        }

        if (room.ListingStatus == "REJECTED")
        {
            ResetRejectedRoom(room);
        }

        room.UpdatedAt =
            DateTime.UtcNow;

        await _dbContext.SaveChangesAsync(
            cancellationToken);
    }

    private async Task<Room> GetOwnedRoomAsync(
        long userId,
        long roomId,
        bool requireEditable,
        CancellationToken cancellationToken)
    {
        var landlordId =
            await _dbContext.LandlordProfiles
                .AsNoTracking()
                .Where(x => x.UserId == userId)
                .Select(
                    x => (long?)x.LandlordId)
                .SingleOrDefaultAsync(
                    cancellationToken);

        if (!landlordId.HasValue)
        {
            throw new ForbiddenException(
                "LANDLORD_PROFILE_REQUIRED",
                "Tài khoản chưa có hồ sơ chủ trọ.");
        }

        var room =
            await _dbContext.Rooms
                .Include(x => x.Property)
                .SingleOrDefaultAsync(
                    x =>
                        x.RoomId == roomId &&
                        x.Property.LandlordId ==
                            landlordId.Value,
                    cancellationToken);

        if (room is null)
        {
            throw new NotFoundException(
                "ROOM_NOT_FOUND",
                "Không tìm thấy phòng.");
        }

        if (requireEditable &&
            room.ListingStatus == "PENDING")
        {
            throw new ConflictException(
                "ROOM_PENDING_REVIEW",
                "Tin phòng đang chờ duyệt và không thể chỉnh sửa.");
        }

        return room;
    }

    private IQueryable<RoomImageResponse>
        QueryImages(long roomId)
    {
        return _dbContext.RoomImages
            .AsNoTracking()
            .Where(x => x.RoomId == roomId)
            .OrderByDescending(
                x => x.IsPrimary)
            .ThenBy(x => x.SortOrder)
            .ThenBy(x => x.ImageId)
            .Select(
                x =>
                    new RoomImageResponse
                    {
                        ImageId =
                            x.ImageId,

                        RoomId =
                            x.RoomId,

                        ImageUrl =
                            x.ImageUrl,

                        IsPrimary =
                            x.IsPrimary,

                        SortOrder =
                            x.SortOrder,

                        CreatedAt =
                            x.CreatedAt
                    });
    }

    private static void ValidateFile(
        IFormFile file)
    {
        if (file.Length <= 0)
        {
            throw new BadRequestException(
                "EMPTY_IMAGE",
                "Ảnh tải lên không có dữ liệu.");
        }

        if (file.Length > MaxImageSize)
        {
            throw new BadRequestException(
                "IMAGE_TOO_LARGE",
                "Mỗi ảnh không được vượt quá 10 MB.");
        }

        if (!AllowedContentTypes.Contains(
            file.ContentType.ToLowerInvariant()))
        {
            throw new BadRequestException(
                "INVALID_IMAGE_TYPE",
                "Chỉ hỗ trợ ảnh JPEG, PNG hoặc WEBP.");
        }
    }

    private static void ResetRejectedRoom(
        Room room)
    {
        room.ListingStatus =
            "DRAFT";

        room.RejectionReason =
            null;

        room.ReviewedAt =
            null;

        room.ReviewedByUserId =
            null;
    }
}