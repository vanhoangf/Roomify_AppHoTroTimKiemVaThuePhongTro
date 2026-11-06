using Microsoft.EntityFrameworkCore;
using Roomify.API.Data;
using Roomify.API.DTOs.Rentals;
using Roomify.API.Entities;
using Roomify.API.Exceptions;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Services.Implementations;

public class RentalRequestService : IRentalRequestService
{
    private static readonly HashSet<string> ValidStatuses =
        new(StringComparer.OrdinalIgnoreCase)
        {
            "PENDING",
            "APPROVED",
            "REJECTED",
            "CANCELLED"
        };

    private readonly RoomifyDbContext _db;

    public RentalRequestService(RoomifyDbContext db)
    {
        _db = db;
    }

    public async Task<RentalRequestResponse> CreateAsync(
        long userId,
        long roomId,
        CreateRentalRequestRequest request)
    {
        var student = await _db.StudentProfiles
            .AsNoTracking()
            .FirstOrDefaultAsync(x => x.UserId == userId)
            ?? throw new NotFoundException(
                "STUDENT_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ sinh viên.");

        var room = await _db.Rooms
            .Include(x => x.Property)
            .FirstOrDefaultAsync(x =>
                x.RoomId == roomId &&
                x.ListingStatus == "PUBLISHED" &&
                x.Property.Status == "ACTIVE")
            ?? throw new NotFoundException(
                "ROOM_NOT_FOUND",
                "Không tìm thấy phòng đang công khai.");

        if (room.AvailabilityStatus != "AVAILABLE")
        {
            throw new ConflictException(
                "ROOM_NOT_AVAILABLE",
                "Phòng hiện không còn cho thuê.");
        }

        var today = DateOnly.FromDateTime(DateTime.UtcNow);

        if (request.RequestedMoveInDate < today)
        {
            throw new BadRequestException(
                "INVALID_MOVE_IN_DATE",
                "Ngày dự kiến chuyển vào không được nằm trong quá khứ.");
        }

        if (request.DurationMonths is <= 0)
        {
            throw new BadRequestException(
                "INVALID_DURATION",
                "Thời hạn thuê phải lớn hơn 0 tháng.");
        }

        var alreadyPending = await _db.RentalRequests
            .AnyAsync(x =>
                x.StudentId == student.StudentId &&
                x.RoomId == roomId &&
                x.Status == "PENDING");

        if (alreadyPending)
        {
            throw new ConflictException(
                "RENTAL_REQUEST_ALREADY_PENDING",
                "Bạn đã có yêu cầu thuê đang chờ xử lý với phòng này.");
        }

        var now = DateTime.UtcNow;

        var entity = new RentalRequest
        {
            RoomId = roomId,
            StudentId = student.StudentId,
            RequestedMoveInDate = request.RequestedMoveInDate,
            DurationMonths = request.DurationMonths,
            Note = Normalize(request.Note),
            Status = "PENDING",
            LandlordNote = null,
            RespondedAt = null,
            CreatedAt = now,
            UpdatedAt = now
        };

        _db.RentalRequests.Add(entity);

        try
        {
            await _db.SaveChangesAsync();
        }
        catch (DbUpdateException)
        {
            // Database cũng có unique filtered index:
            // StudentID + RoomID WHERE Status = PENDING.
            // Giữ lớp bảo vệ này cho trường hợp hai request chạy đồng thời.
            throw new ConflictException(
                "RENTAL_REQUEST_ALREADY_PENDING",
                "Bạn đã có yêu cầu thuê đang chờ xử lý với phòng này.");
        }

        return await GetStudentDetailAsync(
            userId,
            entity.RentalRequestId);
    }

    public async Task<RentalRequestListResponse> GetStudentListAsync(
        long userId,
        string? status,
        int page,
        int pageSize)
    {
        ValidatePagination(page, pageSize);

        var normalizedStatus = ValidateStatus(status);

        var student = await _db.StudentProfiles
            .AsNoTracking()
            .FirstOrDefaultAsync(x => x.UserId == userId)
            ?? throw new NotFoundException(
                "STUDENT_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ sinh viên.");

        var query = BaseQuery()
            .Where(x => x.StudentId == student.StudentId);

        if (normalizedStatus is not null)
        {
            query = query.Where(x => x.Status == normalizedStatus);
        }

        return await BuildPagedResponseAsync(
            query,
            page,
            pageSize);
    }

    public async Task<RentalRequestResponse> GetStudentDetailAsync(
        long userId,
        long rentalRequestId)
    {
        var student = await _db.StudentProfiles
            .AsNoTracking()
            .FirstOrDefaultAsync(x => x.UserId == userId)
            ?? throw new NotFoundException(
                "STUDENT_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ sinh viên.");

        var entity = await BaseQuery()
            .FirstOrDefaultAsync(x =>
                x.RentalRequestId == rentalRequestId &&
                x.StudentId == student.StudentId)
            ?? throw new NotFoundException(
                "RENTAL_REQUEST_NOT_FOUND",
                "Không tìm thấy yêu cầu thuê phòng.");

        return ToResponse(entity);
    }

    public async Task<RentalRequestResponse> CancelByStudentAsync(
        long userId,
        long rentalRequestId)
    {
        var student = await _db.StudentProfiles
            .AsNoTracking()
            .FirstOrDefaultAsync(x => x.UserId == userId)
            ?? throw new NotFoundException(
                "STUDENT_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ sinh viên.");

        var entity = await BaseQuery()
            .FirstOrDefaultAsync(x =>
                x.RentalRequestId == rentalRequestId &&
                x.StudentId == student.StudentId)
            ?? throw new NotFoundException(
                "RENTAL_REQUEST_NOT_FOUND",
                "Không tìm thấy yêu cầu thuê phòng.");

        if (entity.Status != "PENDING")
        {
            throw new ConflictException(
                "INVALID_RENTAL_REQUEST_STATUS",
                "Chỉ có thể hủy yêu cầu thuê đang chờ xử lý.");
        }

        entity.Status = "CANCELLED";
        entity.UpdatedAt = DateTime.UtcNow;

        await _db.SaveChangesAsync();

        return ToResponse(entity);
    }

    public async Task<RentalRequestListResponse> GetLandlordListAsync(
        long userId,
        string? status,
        int page,
        int pageSize)
    {
        ValidatePagination(page, pageSize);

        var normalizedStatus = ValidateStatus(status);

        var landlord = await _db.LandlordProfiles
            .AsNoTracking()
            .FirstOrDefaultAsync(x => x.UserId == userId)
            ?? throw new NotFoundException(
                "LANDLORD_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ chủ trọ.");

        var query = BaseQuery()
            .Where(x =>
                x.Room.Property.LandlordId == landlord.LandlordId);

        if (normalizedStatus is not null)
        {
            query = query.Where(x => x.Status == normalizedStatus);
        }

        return await BuildPagedResponseAsync(
            query,
            page,
            pageSize);
    }

    public async Task<RentalRequestResponse> GetLandlordDetailAsync(
        long userId,
        long rentalRequestId)
    {
        var landlord = await _db.LandlordProfiles
            .AsNoTracking()
            .FirstOrDefaultAsync(x => x.UserId == userId)
            ?? throw new NotFoundException(
                "LANDLORD_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ chủ trọ.");

        var entity = await BaseQuery()
            .FirstOrDefaultAsync(x =>
                x.RentalRequestId == rentalRequestId &&
                x.Room.Property.LandlordId == landlord.LandlordId)
            ?? throw new NotFoundException(
                "RENTAL_REQUEST_NOT_FOUND",
                "Không tìm thấy yêu cầu thuê phòng.");

        return ToResponse(entity);
    }

    public async Task<RentalRequestResponse> ApproveAsync(
        long userId,
        long rentalRequestId,
        RentalRequestDecisionRequest request)
    {
        var landlord = await GetLandlordAsync(userId);

        var entity = await BaseQuery()
            .FirstOrDefaultAsync(x =>
                x.RentalRequestId == rentalRequestId &&
                x.Room.Property.LandlordId == landlord.LandlordId)
            ?? throw new NotFoundException(
                "RENTAL_REQUEST_NOT_FOUND",
                "Không tìm thấy yêu cầu thuê phòng.");

        if (entity.Status != "PENDING")
        {
            throw new ConflictException(
                "INVALID_RENTAL_REQUEST_STATUS",
                "Chỉ có thể duyệt yêu cầu thuê đang chờ xử lý.");
        }

        if (entity.Room.ListingStatus != "PUBLISHED" ||
            entity.Room.Property.Status != "ACTIVE")
        {
            throw new ConflictException(
                "ROOM_NOT_PUBLIC",
                "Phòng hiện không còn được công khai.");
        }

        if (entity.Room.AvailabilityStatus != "AVAILABLE")
        {
            throw new ConflictException(
                "ROOM_NOT_AVAILABLE",
                "Phòng hiện không còn cho thuê.");
        }

        var now = DateTime.UtcNow;

        entity.Status = "APPROVED";
        entity.LandlordNote = Normalize(request.Note);
        entity.RespondedAt = now;
        entity.UpdatedAt = now;

        await _db.SaveChangesAsync();

        // Không tạo RentalContract tại đây.
        // Bước 17 sẽ dùng RentalRequest APPROVED để tạo hợp đồng.

        return ToResponse(entity);
    }

    public async Task<RentalRequestResponse> RejectAsync(
        long userId,
        long rentalRequestId,
        RentalRequestDecisionRequest request)
    {
        var landlord = await GetLandlordAsync(userId);

        var entity = await BaseQuery()
            .FirstOrDefaultAsync(x =>
                x.RentalRequestId == rentalRequestId &&
                x.Room.Property.LandlordId == landlord.LandlordId)
            ?? throw new NotFoundException(
                "RENTAL_REQUEST_NOT_FOUND",
                "Không tìm thấy yêu cầu thuê phòng.");

        if (entity.Status != "PENDING")
        {
            throw new ConflictException(
                "INVALID_RENTAL_REQUEST_STATUS",
                "Chỉ có thể từ chối yêu cầu thuê đang chờ xử lý.");
        }

        var landlordNote = Normalize(request.Note);

        if (string.IsNullOrWhiteSpace(landlordNote))
        {
            throw new BadRequestException(
                "REJECTION_REASON_REQUIRED",
                "Vui lòng nhập lý do từ chối yêu cầu thuê.");
        }

        var now = DateTime.UtcNow;

        entity.Status = "REJECTED";
        entity.LandlordNote = landlordNote;
        entity.RespondedAt = now;
        entity.UpdatedAt = now;

        await _db.SaveChangesAsync();

        return ToResponse(entity);
    }

    private IQueryable<RentalRequest> BaseQuery()
    {
        return _db.RentalRequests
            .Include(x => x.Room)
                .ThenInclude(x => x.Property)
            .Include(x => x.Student)
                .ThenInclude(x => x.User);
    }

    private async Task<LandlordProfile> GetLandlordAsync(long userId)
    {
        return await _db.LandlordProfiles
            .AsNoTracking()
            .FirstOrDefaultAsync(x => x.UserId == userId)
            ?? throw new NotFoundException(
                "LANDLORD_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ chủ trọ.");
    }

    private async Task<RentalRequestListResponse> BuildPagedResponseAsync(
        IQueryable<RentalRequest> query,
        int page,
        int pageSize)
    {
        var totalCount = await query.CountAsync();

        var entities = await query
            .OrderByDescending(x => x.CreatedAt)
            .ThenByDescending(x => x.RentalRequestId)
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .ToListAsync();

        var totalPages = totalCount == 0
            ? 0
            : (int)Math.Ceiling(totalCount / (double)pageSize);

        return new RentalRequestListResponse
        {
            Items = entities
                .Select(ToResponse)
                .ToList(),

            TotalCount = totalCount,
            Page = page,
            PageSize = pageSize,
            TotalPages = totalPages,
            HasNextPage = page < totalPages,
            HasPreviousPage = page > 1
        };
    }

    private static RentalRequestResponse ToResponse(
        RentalRequest entity)
    {
        return new RentalRequestResponse
        {
            RentalRequestId = entity.RentalRequestId,
            RoomId = entity.RoomId,
            RoomTitle = entity.Room.Title ?? string.Empty,

            PropertyId = entity.Room.PropertyId,
            PropertyName =
                entity.Room.Property.PropertyName ?? string.Empty,

            StudentId = entity.StudentId,
            StudentName =
                entity.Student.User.FullName ?? string.Empty,
            StudentPhone =
                entity.Student.User.Phone,

            RequestedMoveInDate =
                entity.RequestedMoveInDate,

            DurationMonths =
                entity.DurationMonths,

            Note =
                entity.Note,

            Status =
                entity.Status,

            LandlordNote =
                entity.LandlordNote,

            RespondedAt =
                entity.RespondedAt,

            CreatedAt =
                entity.CreatedAt,

            UpdatedAt =
                entity.UpdatedAt
        };
    }

    private static void ValidatePagination(
        int page,
        int pageSize)
    {
        if (page < 1 || page > 100000)
        {
            throw new BadRequestException(
                "INVALID_PAGE",
                "Page phải nằm trong khoảng từ 1 đến 100000.");
        }

        if (pageSize < 1 || pageSize > 50)
        {
            throw new BadRequestException(
                "INVALID_PAGE_SIZE",
                "PageSize phải nằm trong khoảng từ 1 đến 50.");
        }
    }

    private static string? ValidateStatus(string? status)
    {
        if (string.IsNullOrWhiteSpace(status))
        {
            return null;
        }

        var normalized = status
            .Trim()
            .ToUpperInvariant();

        if (!ValidStatuses.Contains(normalized))
        {
            throw new BadRequestException(
                "INVALID_RENTAL_REQUEST_STATUS",
                "Trạng thái yêu cầu thuê không hợp lệ.");
        }

        return normalized;
    }

    private static string? Normalize(string? value)
    {
        return string.IsNullOrWhiteSpace(value)
            ? null
            : value.Trim();
    }
}