using Microsoft.EntityFrameworkCore;
using Roomify.API.Data;
using Roomify.API.DTOs.Admin.Rooms;
using Roomify.API.Entities;
using Roomify.API.Exceptions;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Services.Implementations;

public class AdminRoomService : IAdminRoomService
{
    private readonly RoomifyDbContext _db;

    private static readonly HashSet<string> ValidListingStatuses =
        new(StringComparer.OrdinalIgnoreCase)
        {
            "DRAFT",
            "PENDING",
            "PUBLISHED",
            "REJECTED",
            "HIDDEN"
        };

    public AdminRoomService(RoomifyDbContext db)
    {
        _db = db;
    }

    // ============================================================
    // GET ROOM LIST
    // ============================================================

    public async Task<AdminRoomListResponse> GetRoomsAsync(
        string? listingStatus,
        string? search,
        int page,
        int pageSize)
    {
        ValidatePaging(page, pageSize);

        var normalizedStatus =
            ValidateListingStatusFilter(listingStatus);

        var normalizedSearch =
            NormalizeOptional(search);

        var query = _db.Rooms
            .AsNoTracking()
            .AsQueryable();

        if (normalizedStatus is not null)
        {
            query = query.Where(x =>
                x.ListingStatus == normalizedStatus);
        }

        if (normalizedSearch is not null)
        {
            query = query.Where(x =>
                x.Title.Contains(normalizedSearch) ||

                (x.RoomNumber != null &&
                 x.RoomNumber.Contains(normalizedSearch)) ||

                x.Property.PropertyName.Contains(normalizedSearch) ||

                x.Property.Landlord.User.FullName.Contains(
                    normalizedSearch) ||

                x.Property.Landlord.User.Email.Contains(
                    normalizedSearch));
        }

        var totalCount =
            await query.CountAsync();

        var items = await ProjectListResponse(query)
            .OrderByDescending(x => x.CreatedAt)
            .ThenByDescending(x => x.RoomId)
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .ToListAsync();

        return BuildListResponse(
            items,
            totalCount,
            page,
            pageSize);
    }

    // ============================================================
    // GET ROOM DETAIL
    // ============================================================

    public async Task<AdminRoomResponse> GetRoomByIdAsync(
    long roomId)
    {
        var result = await ProjectDetailResponse(
                _db.Rooms
                    .AsNoTracking()
                    .AsSplitQuery()
                    .Where(x =>
                        x.RoomId == roomId))
            .FirstOrDefaultAsync();

        if (result is null)
        {
            throw new NotFoundException(
                "ROOM_NOT_FOUND",
                "Không tìm thấy phòng.");
        }

        return result;
    }

    // ============================================================
    // APPROVE ROOM
    // PENDING -> PUBLISHED
    // ============================================================

    public async Task<AdminRoomResponse> ApproveAsync(
        long adminUserId,
        long roomId)
    {
        var room = await _db.Rooms
            .FirstOrDefaultAsync(x =>
                x.RoomId == roomId);

        if (room is null)
        {
            throw new NotFoundException(
                "ROOM_NOT_FOUND",
                "Không tìm thấy phòng.");
        }

        EnsurePending(room);

        var now = DateTime.UtcNow;

        room.ListingStatus = "PUBLISHED";

        room.ReviewedByUserId =
            adminUserId;

        room.ReviewedAt =
            now;

        room.RejectionReason =
            null;

        room.PublishedAt =
            now;

        room.UpdatedAt =
            now;

        await _db.SaveChangesAsync();

        return await GetRoomByIdAsync(
            roomId);
    }

    // ============================================================
    // REJECT ROOM
    // PENDING -> REJECTED
    // ============================================================

    public async Task<AdminRoomResponse> RejectAsync(
        long adminUserId,
        long roomId,
        RejectRoomRequest request)
    {
        var room = await _db.Rooms
            .FirstOrDefaultAsync(x =>
                x.RoomId == roomId);

        if (room is null)
        {
            throw new NotFoundException(
                "ROOM_NOT_FOUND",
                "Không tìm thấy phòng.");
        }

        EnsurePending(room);

        var reason =
            NormalizeOptional(request.Reason);

        if (reason is null)
        {
            throw new BadRequestException(
                "REJECTION_REASON_REQUIRED",
                "Vui lòng nhập lý do từ chối tin phòng.");
        }

        var now = DateTime.UtcNow;

        room.ListingStatus =
            "REJECTED";

        room.ReviewedByUserId =
            adminUserId;

        room.ReviewedAt =
            now;

        room.RejectionReason =
            reason;

        room.PublishedAt =
            null;

        room.UpdatedAt =
            now;

        await _db.SaveChangesAsync();

        return await GetRoomByIdAsync(
            roomId);
    }

    // ============================================================
    // PROJECTION
    // ============================================================

    private static IQueryable<AdminRoomResponse>
    ProjectListResponse(
        IQueryable<Room> query)
    {
        return query.Select(
            x => new AdminRoomResponse
            {
                RoomId =
                    x.RoomId,

                PropertyId =
                    x.PropertyId,

                PropertyName =
                    x.Property.PropertyName,


                LandlordId =
                    x.Property.LandlordId,

                LandlordUserId =
                    x.Property.Landlord.UserId,

                LandlordName =
                    x.Property.Landlord.User.FullName,

                LandlordEmail =
                    x.Property.Landlord.User.Email,


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


                ListingStatus =
                    x.ListingStatus,

                AvailabilityStatus =
                    x.AvailabilityStatus,

                AvailableFrom =
                    x.AvailableFrom,

                ViewCount =
                    x.ViewCount,


                ReviewedByUserId =
                    x.ReviewedByUserId,

                ReviewedByUserName =
                    x.ReviewedByUser != null
                        ? x.ReviewedByUser.FullName
                        : null,

                ReviewedAt =
                    x.ReviewedAt,

                RejectionReason =
                    x.RejectionReason,

                PublishedAt =
                    x.PublishedAt,


                CreatedAt =
                    x.CreatedAt,

                UpdatedAt =
                    x.UpdatedAt
            });
    }

    private static IQueryable<AdminRoomResponse>
    ProjectDetailResponse(
        IQueryable<Room> query)
    {
        return query.Select(
            x => new AdminRoomResponse
            {
                // ====================================================
                // ROOM
                // ====================================================

                RoomId =
                    x.RoomId,

                PropertyId =
                    x.PropertyId,

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


                // ====================================================
                // PROPERTY
                // ====================================================

                PropertyName =
                    x.Property.PropertyName,

                PropertyDescription =
                    x.Property.Description,

                PropertyTypeId =
                    x.Property.PropertyTypeId,

                PropertyTypeName =
                    x.Property.PropertyType.TypeName,

                PropertyStatus =
                    x.Property.Status,


                // ====================================================
                // LOCATION
                // ====================================================

                ProvinceId =
                    x.Property.ProvinceId,

                ProvinceName =
                    x.Property.Province.ProvinceName,

                WardId =
                    x.Property.WardId,

                WardName =
                    x.Property.Ward.WardName,

                AddressLine =
                    x.Property.AddressLine,

                Latitude =
                    x.Property.Latitude,

                Longitude =
                    x.Property.Longitude,


                // ====================================================
                // LANDLORD
                // ====================================================

                LandlordId =
                    x.Property.LandlordId,

                LandlordUserId =
                    x.Property.Landlord.UserId,

                LandlordName =
                    x.Property.Landlord.User.FullName,

                LandlordEmail =
                    x.Property.Landlord.User.Email,

                LandlordPhone =
                    x.Property.Landlord.User.Phone,

                LandlordIsVerified =
                    x.Property.Landlord.IsVerified,


                // ====================================================
                // REVIEW
                // ====================================================

                ReviewedByUserId =
                    x.ReviewedByUserId,

                ReviewedByUserName =
                    x.ReviewedByUser != null
                        ? x.ReviewedByUser.FullName
                        : null,

                ReviewedAt =
                    x.ReviewedAt,

                RejectionReason =
                    x.RejectionReason,

                PublishedAt =
                    x.PublishedAt,


                // ====================================================
                // INTERACTION
                // ====================================================

                SavedCount =
                    x.SavedRooms.Count(),

                TotalReports =
                    x.Reports.Count(),

                OpenReports =
                    x.Reports.Count(r =>
                        r.Status == "OPEN"),


                // ====================================================
                // PROPERTY STATISTICS
                // ====================================================

                PropertyTotalRooms =
                    x.Property.Rooms.Count(),

                PropertyAvailableRooms =
                    x.Property.Rooms.Count(r =>
                        r.AvailabilityStatus ==
                        "AVAILABLE"),

                PropertyRentedRooms =
                    x.Property.Rooms.Count(r =>
                        r.AvailabilityStatus ==
                        "RENTED"),


                // ====================================================
                // IMAGES
                // ====================================================

                Images =
                    x.RoomImages
                        .OrderByDescending(i =>
                            i.IsPrimary)
                        .ThenBy(i =>
                            i.SortOrder)
                        .ThenBy(i =>
                            i.ImageId)
                        .Select(i =>
                            new AdminRoomImageResponse
                            {
                                ImageId =
                                    i.ImageId,

                                ImageUrl =
                                    i.ImageUrl,

                                IsPrimary =
                                    i.IsPrimary,

                                SortOrder =
                                    i.SortOrder
                            })
                        .ToList(),


                // ====================================================
                // AMENITIES
                // ====================================================

                Amenities =
                    x.Amenities
                        .Where(a =>
                            a.IsActive)
                        .OrderBy(a =>
                            a.AmenityName)
                        .Select(a =>
                            new AdminRoomAmenityResponse
                            {
                                AmenityId =
                                    a.AmenityId,

                                AmenityName =
                                    a.AmenityName,

                                IconUrl =
                                    a.IconUrl
                            })
                        .ToList(),


                // ====================================================
                // REPORTS
                // ====================================================

                RecentReports =
                    x.Reports
                        .OrderByDescending(r =>
                            r.CreatedAt)
                        .ThenByDescending(r =>
                            r.ReportId)
                        .Take(5)
                        .Select(r =>
                            new AdminRoomReportResponse
                            {
                                ReportId =
                                    r.ReportId,

                                ReporterUserId =
                                    r.ReporterUserId,

                                ReporterName =
                                    r.ReporterUser.FullName,

                                ReporterEmail =
                                    r.ReporterUser.Email,

                                Reason =
                                    r.Reason,

                                Description =
                                    r.Description,

                                Status =
                                    r.Status,

                                CreatedAt =
                                    r.CreatedAt
                            })
                        .ToList(),


                // ====================================================
                // TIME
                // ====================================================

                CreatedAt =
                    x.CreatedAt,

                UpdatedAt =
                    x.UpdatedAt
            });
    }

    // ============================================================
    // BUSINESS RULE
    // ============================================================

    private static void EnsurePending(
        Room room)
    {
        if (!string.Equals(
                room.ListingStatus,
                "PENDING",
                StringComparison.OrdinalIgnoreCase))
        {
            throw new ConflictException(
                "ROOM_NOT_PENDING_REVIEW",
                "Chỉ có thể duyệt hoặc từ chối tin phòng đang chờ xét duyệt.");
        }
    }

    // ============================================================
    // FILTER VALIDATION
    // ============================================================

    private static string? ValidateListingStatusFilter(
        string? listingStatus)
    {
        var value =
            NormalizeOptional(listingStatus);

        if (value is null)
        {
            return null;
        }

        if (!ValidListingStatuses.Contains(value))
        {
            throw new BadRequestException(
                "INVALID_LISTING_STATUS",
                "Trạng thái tin phòng không hợp lệ.");
        }

        return value.ToUpperInvariant();
    }

    // ============================================================
    // PAGING
    // ============================================================

    private static void ValidatePaging(
        int page,
        int pageSize)
    {
        if (page < 1 ||
            page > 100000)
        {
            throw new BadRequestException(
                "INVALID_PAGE",
                "Page phải nằm trong khoảng từ 1 đến 100000.");
        }

        if (pageSize < 1 ||
            pageSize > 50)
        {
            throw new BadRequestException(
                "INVALID_PAGE_SIZE",
                "PageSize phải nằm trong khoảng từ 1 đến 50.");
        }
    }

    // ============================================================
    // BUILD LIST RESPONSE
    // ============================================================

    private static AdminRoomListResponse BuildListResponse(
        List<AdminRoomResponse> items,
        int totalCount,
        int page,
        int pageSize)
    {
        var totalPages =
            totalCount == 0
                ? 0
                : (int)Math.Ceiling(
                    totalCount /
                    (double)pageSize);

        return new AdminRoomListResponse
        {
            Items = items,

            TotalCount =
                totalCount,

            Page =
                page,

            PageSize =
                pageSize,

            TotalPages =
                totalPages,

            HasNextPage =
                page < totalPages,

            HasPreviousPage =
                page > 1
        };
    }

    // ============================================================
    // NORMALIZATION
    // ============================================================

    private static string? NormalizeOptional(
        string? value)
    {
        if (string.IsNullOrWhiteSpace(value))
        {
            return null;
        }

        return value.Trim();
    }
}