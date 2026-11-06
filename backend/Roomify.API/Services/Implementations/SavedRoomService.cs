
using Microsoft.Data.SqlClient;
using Microsoft.EntityFrameworkCore;

using Roomify.API.Data;
using Roomify.API.DTOs.Student;
using Roomify.API.Entities;
using Roomify.API.Exceptions;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Services.Implementations;

public class SavedRoomService : ISavedRoomService
{
    private readonly RoomifyDbContext _dbContext;

    public SavedRoomService(RoomifyDbContext dbContext)
    {
        _dbContext = dbContext;
    }

    // ==================================================
    // GET SAVED ROOMS
    // ==================================================

    public async Task<IReadOnlyList<SavedRoomResponse>>
        GetSavedRoomsAsync(
            long userId,
            CancellationToken cancellationToken = default)
    {
        var studentId = await GetStudentIdAsync(
            userId, cancellationToken);

        return await QuerySavedRooms(studentId)
            .ToListAsync(cancellationToken);
    }

    // ==================================================
    // SAVE ROOM
    // ==================================================

    public async Task<SavedRoomResponse> SaveRoomAsync(
        long userId,
        long roomId,
        CancellationToken cancellationToken = default)
    {
        var studentId = await GetStudentIdAsync(
            userId, cancellationToken);

        var roomExists = await _dbContext.Rooms
            .AsNoTracking()
            .AnyAsync(x =>
                x.RoomId == roomId &&
                x.ListingStatus == "PUBLISHED" &&
                x.Property.Status == "ACTIVE",
                cancellationToken);

        if (!roomExists)
        {
            throw new NotFoundException(
                "ROOM_NOT_FOUND",
                "Không tìm thấy phòng đang công khai.");
        }

        var alreadySaved = await _dbContext.SavedRooms
            .AnyAsync(x =>
                x.StudentId == studentId &&
                x.RoomId == roomId,
                cancellationToken);

        if (alreadySaved)
        {
            throw new ConflictException(
                "ROOM_ALREADY_SAVED",
                "Bạn đã lưu phòng này trước đó.");
        }

        var savedRoom = new SavedRoom
        {
            StudentId = studentId,
            RoomId = roomId,
            SavedAt = DateTime.UtcNow
        };

        _dbContext.SavedRooms.Add(savedRoom);

        try
        {
            await _dbContext.SaveChangesAsync(
                cancellationToken);
        }
        catch (DbUpdateException ex)
            when (ex.InnerException is SqlException sql &&
                  (sql.Number == 2601 || sql.Number == 2627))
        {
            throw new ConflictException(
                "ROOM_ALREADY_SAVED",
                "Bạn đã lưu phòng này trước đó.");
        }

        return await QuerySavedRooms(studentId)
            .SingleAsync(
                x => x.RoomId == roomId,
                cancellationToken);
    }

    // ==================================================
    // DELETE SAVED ROOM
    // ==================================================

    public async Task DeleteSavedRoomAsync(
        long userId,
        long roomId,
        CancellationToken cancellationToken = default)
    {
        var studentId = await GetStudentIdAsync(
            userId, cancellationToken);

        var savedRoom = await _dbContext.SavedRooms
            .SingleOrDefaultAsync(
                x => x.StudentId == studentId &&
                     x.RoomId == roomId,
                cancellationToken);

        if (savedRoom is null)
        {
            throw new NotFoundException(
                "SAVED_ROOM_NOT_FOUND",
                "Phòng này không có trong danh sách đã lưu.");
        }

        _dbContext.SavedRooms.Remove(savedRoom);

        await _dbContext.SaveChangesAsync(
            cancellationToken);
    }

    // ==================================================
    // GET STUDENT PROFILE
    // ==================================================

    private async Task<long> GetStudentIdAsync(
        long userId,
        CancellationToken cancellationToken)
    {
        var studentId = await _dbContext.StudentProfiles
            .AsNoTracking()
            .Where(x => x.UserId == userId)
            .Select(x => (long?)x.StudentId)
            .SingleOrDefaultAsync(cancellationToken);

        if (!studentId.HasValue)
        {
            throw new ForbiddenException(
                "STUDENT_PROFILE_REQUIRED",
                "Tài khoản chưa có hồ sơ sinh viên.");
        }

        return studentId.Value;
    }

    // ==================================================
    // PUBLIC SAVED ROOMS QUERY
    // ==================================================

    private IQueryable<SavedRoomResponse> QuerySavedRooms(
        long studentId)
    {
        return _dbContext.SavedRooms
            .AsNoTracking()
            .Where(x =>
                x.StudentId == studentId &&
                x.Room.ListingStatus == "PUBLISHED" &&
                x.Room.Property.Status == "ACTIVE")
            .OrderByDescending(x => x.SavedAt)
            .ThenByDescending(x => x.RoomId)
            .Select(x => new SavedRoomResponse
            {
                RoomId = x.RoomId,

                PropertyId = x.Room.PropertyId,

                PropertyName =
                    x.Room.Property.PropertyName,

                Title = x.Room.Title,

                RentPrice = x.Room.RentPrice,

                AreaM2 = x.Room.AreaM2,

                AvailabilityStatus =
                    x.Room.AvailabilityStatus,

                AddressLine =
                    x.Room.Property.AddressLine,

                ProvinceName =
                    x.Room.Property.Province.ProvinceName,

                WardName =
                    x.Room.Property.Ward.WardName,

                ThumbnailUrl = x.Room.RoomImages
                    .OrderByDescending(i => i.IsPrimary)
                    .ThenBy(i => i.SortOrder)
                    .ThenBy(i => i.ImageId)
                    .Select(i => i.ImageUrl)
                    .FirstOrDefault(),

                SavedAt = x.SavedAt
            });
    }
}
