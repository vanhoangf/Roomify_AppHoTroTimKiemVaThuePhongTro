using Microsoft.EntityFrameworkCore;
using Roomify.API.Data;
using Roomify.API.DTOs.Landlord;
using Roomify.API.Entities;
using Roomify.API.Exceptions;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Services.Implementations;

public class LandlordPropertyService : ILandlordPropertyService
{
    private readonly RoomifyDbContext _dbContext;

    public LandlordPropertyService(
        RoomifyDbContext dbContext)
    {
        _dbContext = dbContext;
    }

    // ======================================================
    // GET MY PROPERTIES
    // ======================================================

    public async Task<IReadOnlyList<PropertyResponse>>
        GetPropertiesAsync(
            long userId,
            CancellationToken cancellationToken = default)
    {
        var landlordId =
            await GetLandlordIdAsync(
                userId,
                cancellationToken);

        return await _dbContext.Properties
            .AsNoTracking()
            .Where(x => x.LandlordId == landlordId)
            .OrderByDescending(x => x.CreatedAt)
            .Select(x => new PropertyResponse
            {
                PropertyId = x.PropertyId,
                PropertyTypeId = x.PropertyTypeId,
                PropertyTypeName = x.PropertyType.TypeName,

                PropertyName = x.PropertyName,
                Description = x.Description,

                ProvinceId = x.ProvinceId,
                ProvinceName = x.Province.ProvinceName,

                WardId = x.WardId,
                WardName = x.Ward.WardName,

                AddressLine = x.AddressLine,
                Latitude = x.Latitude,
                Longitude = x.Longitude,

                Status = x.Status,

                RoomCount = x.Rooms.Count,

                CreatedAt = x.CreatedAt,
                UpdatedAt = x.UpdatedAt
            })
            .ToListAsync(cancellationToken);
    }

    // ======================================================
    // GET PROPERTY
    // ======================================================

    public async Task<PropertyResponse> GetPropertyAsync(
        long userId,
        long propertyId,
        CancellationToken cancellationToken = default)
    {
        var landlordId =
            await GetLandlordIdAsync(
                userId,
                cancellationToken);

        var result =
            await _dbContext.Properties
                .AsNoTracking()
                .Where(
                    x =>
                        x.PropertyId == propertyId &&
                        x.LandlordId == landlordId)
                .Select(x => new PropertyResponse
                {
                    PropertyId = x.PropertyId,
                    PropertyTypeId = x.PropertyTypeId,
                    PropertyTypeName =
                        x.PropertyType.TypeName,

                    PropertyName = x.PropertyName,
                    Description = x.Description,

                    ProvinceId = x.ProvinceId,
                    ProvinceName =
                        x.Province.ProvinceName,

                    WardId = x.WardId,
                    WardName =
                        x.Ward.WardName,

                    AddressLine = x.AddressLine,
                    Latitude = x.Latitude,
                    Longitude = x.Longitude,

                    Status = x.Status,

                    RoomCount = x.Rooms.Count,

                    CreatedAt = x.CreatedAt,
                    UpdatedAt = x.UpdatedAt
                })
                .SingleOrDefaultAsync(
                    cancellationToken);

        if (result is null)
        {
            throw new NotFoundException(
                "PROPERTY_NOT_FOUND",
                "Không tìm thấy khu trọ.");
        }

        return result;
    }

    // ======================================================
    // CREATE PROPERTY
    // ======================================================

    public async Task<PropertyResponse> CreatePropertyAsync(
        long userId,
        CreatePropertyRequest request,
        CancellationToken cancellationToken = default)
    {
        var landlordId =
            await GetLandlordIdAsync(
                userId,
                cancellationToken);

        await ValidatePropertyReferenceDataAsync(
            request.PropertyTypeId,
            request.ProvinceId,
            request.WardId,
            cancellationToken);

        var now = DateTime.UtcNow;

        var property =
            new Property
            {
                LandlordId = landlordId,

                PropertyTypeId =
                    request.PropertyTypeId,

                PropertyName =
                    request.PropertyName.Trim(),

                Description =
                    NormalizeNullable(
                        request.Description),

                ProvinceId =
                    request.ProvinceId,

                WardId =
                    request.WardId,

                AddressLine =
                    request.AddressLine.Trim(),

                Latitude =
                    request.Latitude,

                Longitude =
                    request.Longitude,

                Status =
                    "ACTIVE",

                CreatedAt =
                    now,

                UpdatedAt =
                    now
            };

        _dbContext.Properties.Add(property);

        await _dbContext.SaveChangesAsync(
            cancellationToken);

        return await GetPropertyAsync(
            userId,
            property.PropertyId,
            cancellationToken);
    }

    // ======================================================
    // UPDATE PROPERTY
    // ======================================================

    public async Task<PropertyResponse> UpdatePropertyAsync(
        long userId,
        long propertyId,
        UpdatePropertyRequest request,
        CancellationToken cancellationToken = default)
    {
        var property =
            await GetOwnedPropertyEntityAsync(
                userId,
                propertyId,
                cancellationToken);

        await ValidatePropertyReferenceDataAsync(
            request.PropertyTypeId,
            request.ProvinceId,
            request.WardId,
            cancellationToken);

        property.PropertyTypeId =
            request.PropertyTypeId;

        property.PropertyName =
            request.PropertyName.Trim();

        property.Description =
            NormalizeNullable(
                request.Description);

        property.ProvinceId =
            request.ProvinceId;

        property.WardId =
            request.WardId;

        property.AddressLine =
            request.AddressLine.Trim();

        property.Latitude =
            request.Latitude;

        property.Longitude =
            request.Longitude;

        property.UpdatedAt =
            DateTime.UtcNow;

        await _dbContext.SaveChangesAsync(
            cancellationToken);

        return await GetPropertyAsync(
            userId,
            propertyId,
            cancellationToken);
    }

    // ======================================================
    // PROPERTY STATUS
    // ======================================================

    public async Task<PropertyResponse>
        UpdatePropertyStatusAsync(
            long userId,
            long propertyId,
            UpdatePropertyStatusRequest request,
            CancellationToken cancellationToken = default)
    {
        var status =
            request.Status.Trim().ToUpperInvariant();

        if (status is not ("ACTIVE" or "INACTIVE"))
        {
            throw new BadRequestException(
                "INVALID_PROPERTY_STATUS",
                "Trạng thái khu trọ chỉ có thể là ACTIVE hoặc INACTIVE.");
        }

        var property =
            await GetOwnedPropertyEntityAsync(
                userId,
                propertyId,
                cancellationToken);

        property.Status = status;
        property.UpdatedAt = DateTime.UtcNow;

        await _dbContext.SaveChangesAsync(
            cancellationToken);

        return await GetPropertyAsync(
            userId,
            propertyId,
            cancellationToken);
    }

    // ======================================================
    // GET ROOMS
    // ======================================================

    public async Task<IReadOnlyList<RoomResponse>>
    GetRoomsAsync(
        long userId,
        long propertyId,
        CancellationToken cancellationToken = default)
    {
        var property =
            await GetOwnedPropertyEntityAsync(
                userId,
                propertyId,
                cancellationToken);

        var rooms =
            await _dbContext.Rooms
                .AsNoTracking()
                .Include(x => x.Property)
                .Include(x => x.Amenities)
                .Where(
                    x =>
                        x.PropertyId ==
                        property.PropertyId)
                .OrderByDescending(x => x.CreatedAt)
                .ToListAsync(
                    cancellationToken);

        return rooms
            .Select(MapRoomProjection)
            .ToList();
    }

    // ======================================================
    // GET ROOM
    // ======================================================

    public async Task<RoomResponse> GetRoomAsync(
    long userId,
    long roomId,
    CancellationToken cancellationToken = default)
    {
        var landlordId =
            await GetLandlordIdAsync(
                userId,
                cancellationToken);

        var room =
            await _dbContext.Rooms
                .AsNoTracking()
                .Include(x => x.Property)
                .Include(x => x.Amenities)
                .SingleOrDefaultAsync(
                    x =>
                        x.RoomId == roomId &&
                        x.Property.LandlordId == landlordId,
                    cancellationToken);

        if (room is null)
        {
            throw new NotFoundException(
                "ROOM_NOT_FOUND",
                "Không tìm thấy phòng.");
        }

        return MapRoomProjection(room);
    }

    // ======================================================
    // CREATE ROOM
    // ======================================================

    public async Task<RoomResponse> CreateRoomAsync(
        long userId,
        long propertyId,
        CreateRoomRequest request,
        CancellationToken cancellationToken = default)
    {
        var property =
            await GetOwnedPropertyEntityAsync(
                userId,
                propertyId,
                cancellationToken);

        if (property.Status != "ACTIVE")
        {
            throw new BadRequestException(
                "PROPERTY_INACTIVE",
                "Không thể tạo phòng cho khu trọ đang ngừng hoạt động.");
        }

        ValidateGenderPreference(
            request.GenderPreference);

        await ValidateRoomNumberAsync(
            propertyId,
            request.RoomNumber,
            null,
            cancellationToken);

        var amenities =
            await GetActiveAmenitiesAsync(
                request.AmenityIds,
                cancellationToken);

        var now = DateTime.UtcNow;

        var room =
            new Room
            {
                PropertyId =
                    propertyId,

                RoomNumber =
                    NormalizeNullable(
                        request.RoomNumber),

                Title =
                    request.Title.Trim(),

                Description =
                    NormalizeNullable(
                        request.Description),

                AreaM2 =
                    request.AreaM2,

                RentPrice =
                    request.RentPrice,

                DepositAmount =
                    request.DepositAmount,

                ElectricityPrice =
                    request.ElectricityPrice,

                WaterPrice =
                    request.WaterPrice,

                InternetPrice =
                    request.InternetPrice,

                ServiceFee =
                    request.ServiceFee,

                ParkingFee =
                    request.ParkingFee,

                MaxOccupants =
                    request.MaxOccupants,

                GenderPreference =
                    request.GenderPreference
                        .Trim()
                        .ToUpperInvariant(),

                AvailableFrom =
                    request.AvailableFrom,

                ListingStatus =
                    "DRAFT",

                AvailabilityStatus =
                    "AVAILABLE",

                ViewCount =
                    0,

                CreatedAt =
                    now,

                UpdatedAt =
                    now
            };

        foreach (var amenity in amenities)
        {
            room.Amenities.Add(amenity);
        }

        _dbContext.Rooms.Add(room);

        await _dbContext.SaveChangesAsync(
            cancellationToken);

        return await GetRoomAsync(
            userId,
            room.RoomId,
            cancellationToken);
    }

    // ======================================================
    // UPDATE ROOM
    // ======================================================

    public async Task<RoomResponse> UpdateRoomAsync(
        long userId,
        long roomId,
        UpdateRoomRequest request,
        CancellationToken cancellationToken = default)
    {
        var room =
            await GetOwnedRoomEntityAsync(
                userId,
                roomId,
                cancellationToken);

        // Không cho sửa nội dung khi đang chờ duyệt.
        if (room.ListingStatus == "PENDING")
        {
            throw new ConflictException(
                "ROOM_PENDING_REVIEW",
                "Tin phòng đang chờ duyệt và không thể chỉnh sửa.");
        }

        ValidateGenderPreference(
            request.GenderPreference);

        await ValidateRoomNumberAsync(
            room.PropertyId,
            request.RoomNumber,
            room.RoomId,
            cancellationToken);

        room.RoomNumber =
            NormalizeNullable(
                request.RoomNumber);

        room.Title =
            request.Title.Trim();

        room.Description =
            NormalizeNullable(
                request.Description);

        room.AreaM2 =
            request.AreaM2;

        room.RentPrice =
            request.RentPrice;

        room.DepositAmount =
            request.DepositAmount;

        room.ElectricityPrice =
            request.ElectricityPrice;

        room.WaterPrice =
            request.WaterPrice;

        room.InternetPrice =
            request.InternetPrice;

        room.ServiceFee =
            request.ServiceFee;

        room.ParkingFee =
            request.ParkingFee;

        room.MaxOccupants =
            request.MaxOccupants;

        room.GenderPreference =
            request.GenderPreference
                .Trim()
                .ToUpperInvariant();

        room.AvailableFrom =
            request.AvailableFrom;

        // Nếu tin đã từng bị từ chối và chủ trọ sửa,
        // đưa về DRAFT để chuẩn bị gửi duyệt lại.
        if (room.ListingStatus == "REJECTED")
        {
            room.ListingStatus = "DRAFT";
            room.RejectionReason = null;
            room.ReviewedAt = null;
            room.ReviewedByUserId = null;
        }

        room.UpdatedAt =
            DateTime.UtcNow;

        await _dbContext.SaveChangesAsync(
            cancellationToken);

        return await GetRoomAsync(
            userId,
            roomId,
            cancellationToken);
    }

    // ======================================================
    // UPDATE AMENITIES
    // ======================================================

    public async Task<RoomResponse> UpdateAmenitiesAsync(
    long userId,
    long roomId,
    UpdateRoomAmenitiesRequest request,
    CancellationToken cancellationToken = default)
    {
        var landlordId =
            await GetLandlordIdAsync(
                userId,
                cancellationToken);

        var room =
            await _dbContext.Rooms
                .Include(x => x.Property)
                .Include(x => x.Amenities)
                .SingleOrDefaultAsync(
                    x => x.RoomId == roomId,
                    cancellationToken);

        if (room is null ||
            room.Property.LandlordId != landlordId)
        {
            throw new NotFoundException(
                "ROOM_NOT_FOUND",
                "Không tìm thấy phòng.");
        }

        if (room.ListingStatus == "PENDING")
        {
            throw new ConflictException(
                "ROOM_PENDING_REVIEW",
                "Tin phòng đang chờ duyệt và không thể chỉnh sửa.");
        }

        // Loại bỏ AmenityId trùng nhau
        var requestedIds =
            request.AmenityIds
                .Distinct()
                .ToList();

        // Kiểm tra các tiện ích gửi lên đều tồn tại và đang hoạt động
        var requestedAmenities =
            await GetActiveAmenitiesAsync(
                requestedIds,
                cancellationToken);

        // =====================================================
        // 1. XÓA NHỮNG TIỆN ÍCH KHÔNG CÒN ĐƯỢC CHỌN
        // =====================================================

        var amenitiesToRemove =
            room.Amenities
                .Where(
                    existing =>
                        !requestedIds.Contains(
                            existing.AmenityId))
                .ToList();

        foreach (var amenity in amenitiesToRemove)
        {
            room.Amenities.Remove(amenity);
        }

        // =====================================================
        // 2. CHỈ THÊM NHỮNG TIỆN ÍCH CHƯA CÓ
        // =====================================================

        var existingIds =
            room.Amenities
                .Select(x => x.AmenityId)
                .ToHashSet();

        var amenitiesToAdd =
            requestedAmenities
                .Where(
                    x =>
                        !existingIds.Contains(
                            x.AmenityId))
                .ToList();

        foreach (var amenity in amenitiesToAdd)
        {
            room.Amenities.Add(amenity);
        }

        room.UpdatedAt =
            DateTime.UtcNow;

        // Nếu phòng từng bị từ chối,
        // sau khi chỉnh sửa thì đưa về DRAFT.
        if (room.ListingStatus == "REJECTED")
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

        await _dbContext.SaveChangesAsync(
            cancellationToken);

        return await GetRoomAsync(
            userId,
            roomId,
            cancellationToken);
    }

    // ======================================================
    // AVAILABILITY
    // ======================================================

    public async Task<RoomResponse>
        UpdateAvailabilityAsync(
            long userId,
            long roomId,
            UpdateRoomAvailabilityRequest request,
            CancellationToken cancellationToken = default)
    {
        var status =
            request.AvailabilityStatus
                .Trim()
                .ToUpperInvariant();

        if (status is not (
            "AVAILABLE" or
            "RESERVED" or
            "RENTED"))
        {
            throw new BadRequestException(
                "INVALID_AVAILABILITY_STATUS",
                "Trạng thái phòng phải là AVAILABLE, RESERVED hoặc RENTED.");
        }

        var room =
            await GetOwnedRoomEntityAsync(
                userId,
                roomId,
                cancellationToken);

        room.AvailabilityStatus =
            status;

        room.UpdatedAt =
            DateTime.UtcNow;

        await _dbContext.SaveChangesAsync(
            cancellationToken);

        return await GetRoomAsync(
            userId,
            roomId,
            cancellationToken);
    }

    // ======================================================
    // SUBMIT REVIEW
    // ======================================================

    public async Task<RoomResponse> SubmitForReviewAsync(
        long userId,
        long roomId,
        CancellationToken cancellationToken = default)
    {
        var room =
            await GetOwnedRoomEntityAsync(
                userId,
                roomId,
                cancellationToken);

        if (room.ListingStatus is not (
            "DRAFT" or "REJECTED"))
        {
            throw new ConflictException(
                "INVALID_LISTING_TRANSITION",
                "Chỉ tin DRAFT hoặc REJECTED mới có thể gửi duyệt.");
        }

        room.ListingStatus =
            "PENDING";

        room.RejectionReason =
            null;

        room.ReviewedAt =
            null;

        room.ReviewedByUserId =
            null;

        room.UpdatedAt =
            DateTime.UtcNow;

        await _dbContext.SaveChangesAsync(
            cancellationToken);

        return await GetRoomAsync(
            userId,
            roomId,
            cancellationToken);
    }

    // ======================================================
    // HIDE PUBLISHED ROOM
    // ======================================================

    public async Task<RoomResponse> HideRoomAsync(
        long userId,
        long roomId,
        CancellationToken cancellationToken = default)
    {
        var room =
            await GetOwnedRoomEntityAsync(
                userId,
                roomId,
                cancellationToken);

        if (room.ListingStatus != "PUBLISHED")
        {
            throw new ConflictException(
                "INVALID_LISTING_TRANSITION",
                "Chỉ tin đang PUBLISHED mới có thể ẩn.");
        }

        room.ListingStatus =
            "HIDDEN";

        room.UpdatedAt =
            DateTime.UtcNow;

        await _dbContext.SaveChangesAsync(
            cancellationToken);

        return await GetRoomAsync(
            userId,
            roomId,
            cancellationToken);
    }

    // ======================================================
    // HELPERS
    // ======================================================

    private async Task<long> GetLandlordIdAsync(
        long userId,
        CancellationToken cancellationToken)
    {
        var landlordId =
            await _dbContext.LandlordProfiles
                .AsNoTracking()
                .Where(x => x.UserId == userId)
                .Select(x => (long?)x.LandlordId)
                .SingleOrDefaultAsync(
                    cancellationToken);

        if (!landlordId.HasValue)
        {
            throw new ForbiddenException(
                "LANDLORD_PROFILE_REQUIRED",
                "Tài khoản chưa có hồ sơ chủ trọ.");
        }

        return landlordId.Value;
    }

    private async Task<Property>
        GetOwnedPropertyEntityAsync(
            long userId,
            long propertyId,
            CancellationToken cancellationToken)
    {
        var landlordId =
            await GetLandlordIdAsync(
                userId,
                cancellationToken);

        var property =
            await _dbContext.Properties
                .SingleOrDefaultAsync(
                    x =>
                        x.PropertyId == propertyId &&
                        x.LandlordId == landlordId,
                    cancellationToken);

        if (property is null)
        {
            throw new NotFoundException(
                "PROPERTY_NOT_FOUND",
                "Không tìm thấy khu trọ.");
        }

        return property;
    }

    private async Task<Room>
        GetOwnedRoomEntityAsync(
            long userId,
            long roomId,
            CancellationToken cancellationToken)
    {
        var landlordId =
            await GetLandlordIdAsync(
                userId,
                cancellationToken);

        var room =
            await _dbContext.Rooms
                .Include(x => x.Property)
                .SingleOrDefaultAsync(
                    x =>
                        x.RoomId == roomId &&
                        x.Property.LandlordId ==
                            landlordId,
                    cancellationToken);

        if (room is null)
        {
            throw new NotFoundException(
                "ROOM_NOT_FOUND",
                "Không tìm thấy phòng.");
        }

        return room;
    }

    private async Task EnsureRoomOwnershipAsync(
        long userId,
        long roomId,
        CancellationToken cancellationToken)
    {
        _ = await GetOwnedRoomEntityAsync(
            userId,
            roomId,
            cancellationToken);
    }

    private async Task ValidatePropertyReferenceDataAsync(
        int propertyTypeId,
        int provinceId,
        int wardId,
        CancellationToken cancellationToken)
    {
        var propertyTypeExists =
            await _dbContext.PropertyTypes
                .AnyAsync(
                    x =>
                        x.PropertyTypeId ==
                            propertyTypeId &&
                        x.IsActive,
                    cancellationToken);

        if (!propertyTypeExists)
        {
            throw new BadRequestException(
                "PROPERTY_TYPE_NOT_FOUND",
                "Loại hình khu trọ không tồn tại hoặc đã ngừng hoạt động.");
        }

        var provinceExists =
            await _dbContext.Provinces
                .AnyAsync(
                    x =>
                        x.ProvinceId ==
                            provinceId &&
                        x.IsActive,
                    cancellationToken);

        if (!provinceExists)
        {
            throw new BadRequestException(
                "PROVINCE_NOT_FOUND",
                "Tỉnh/thành phố không tồn tại hoặc đã ngừng hoạt động.");
        }

        var wardExists =
            await _dbContext.Wards
                .AnyAsync(
                    x =>
                        x.WardId == wardId &&
                        x.ProvinceId == provinceId &&
                        x.IsActive,
                    cancellationToken);

        if (!wardExists)
        {
            throw new BadRequestException(
                "WARD_NOT_FOUND",
                "Xã/phường không thuộc tỉnh/thành phố đã chọn hoặc đã ngừng hoạt động.");
        }
    }

    private async Task ValidateRoomNumberAsync(
        long propertyId,
        string? roomNumber,
        long? currentRoomId,
        CancellationToken cancellationToken)
    {
        var normalized =
            NormalizeNullable(roomNumber);

        if (normalized is null)
        {
            return;
        }

        var exists =
            await _dbContext.Rooms
                .AnyAsync(
                    x =>
                        x.PropertyId == propertyId &&
                        x.RoomNumber == normalized &&
                        (!currentRoomId.HasValue ||
                         x.RoomId != currentRoomId.Value),
                    cancellationToken);

        if (exists)
        {
            throw new ConflictException(
                "ROOM_NUMBER_ALREADY_EXISTS",
                "Số phòng đã tồn tại trong khu trọ này.");
        }
    }

    private async Task<List<Amenity>>
        GetActiveAmenitiesAsync(
            IEnumerable<int> amenityIds,
            CancellationToken cancellationToken)
    {
        var ids =
            amenityIds
                .Distinct()
                .ToList();

        if (ids.Count == 0)
        {
            return [];
        }

        var amenities =
            await _dbContext.Amenities
                .Where(
                    x =>
                        ids.Contains(x.AmenityId) &&
                        x.IsActive)
                .ToListAsync(
                    cancellationToken);

        if (amenities.Count != ids.Count)
        {
            throw new BadRequestException(
                "INVALID_AMENITY",
                "Một hoặc nhiều tiện ích không tồn tại hoặc đã ngừng hoạt động.");
        }

        return amenities;
    }

    private static void ValidateGenderPreference(
        string value)
    {
        var normalized =
            value.Trim().ToUpperInvariant();

        if (normalized is not (
            "ALL" or "MALE" or "FEMALE"))
        {
            throw new BadRequestException(
                "INVALID_GENDER_PREFERENCE",
                "Giới tính ưu tiên phải là ALL, MALE hoặc FEMALE.");
        }
    }

    private static string? NormalizeNullable(
        string? value)
    {
        return string.IsNullOrWhiteSpace(value)
            ? null
            : value.Trim();
    }

    private static RoomResponse MapRoomProjection(
        Room x)
    {
        return new RoomResponse
        {
            RoomId = x.RoomId,

            PropertyId = x.PropertyId,

            PropertyName =
                x.Property.PropertyName,

            RoomNumber =
                x.RoomNumber,

            Title =
                x.Title,

            Description =
                x.Description,

            AreaM2 =
                x.AreaM2,

            RentPrice =
                x.RentPrice,

            DepositAmount =
                x.DepositAmount,

            ElectricityPrice =
                x.ElectricityPrice,

            WaterPrice =
                x.WaterPrice,

            InternetPrice =
                x.InternetPrice,

            ServiceFee =
                x.ServiceFee,

            ParkingFee =
                x.ParkingFee,

            MaxOccupants =
                x.MaxOccupants,

            GenderPreference =
                x.GenderPreference,

            AvailableFrom =
                x.AvailableFrom,

            ListingStatus =
                x.ListingStatus,

            AvailabilityStatus =
                x.AvailabilityStatus,

            ViewCount =
                x.ViewCount,

            RejectionReason =
                x.RejectionReason,

            PublishedAt =
                x.PublishedAt,

            Amenities =
                x.Amenities
                    .OrderBy(a => a.AmenityName)
                    .Select(a =>
                        new RoomAmenityResponse
                        {
                            AmenityId =
                                a.AmenityId,

                            AmenityName =
                                a.AmenityName,

                            IconUrl =
                                a.IconUrl
                        })
                    .ToList(),

            CreatedAt =
                x.CreatedAt,

            UpdatedAt =
                x.UpdatedAt
        };
    }
}