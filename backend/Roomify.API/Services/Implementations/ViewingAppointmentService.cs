
using System.Data;
using Microsoft.Data.SqlClient;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Storage;
using Roomify.API.Data;
using Roomify.API.DTOs.Public;
using Roomify.API.DTOs.Student;
using Roomify.API.Entities;
using Roomify.API.Exceptions;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Services.Implementations;

public class ViewingAppointmentService : IViewingAppointmentService
{
    private readonly RoomifyDbContext _db;

    public ViewingAppointmentService(RoomifyDbContext db)
    {
        _db = db;
    }

    // ==================================================
    // STUDENT - CREATE APPOINTMENT
    // ==================================================

    public async Task<ViewingAppointmentResponse> CreateAsync(
        long userId,
        long roomId,
        CreateViewingAppointmentRequest request,
        CancellationToken cancellationToken = default)
    {
        if (request is null)
            throw new BadRequestException(
                "INVALID_REQUEST", "Thiếu dữ liệu đặt lịch.");

        var at = ValidateTime(request.ScheduledAt);
        var note = ValidateNote(request.Note);

        var studentId = await GetProfileIdAsync(
            userId, false, cancellationToken);

        await using var tx = await _db.Database
            .BeginTransactionAsync(cancellationToken);

        await LockActorsAsync(
            studentId, roomId, cancellationToken);

        await RequireAvailableRoomAsync(
            roomId, cancellationToken);

        var active = await _db.ViewingAppointments
            .AsNoTracking()
            .AnyAsync(x =>
                x.RoomId == roomId &&
                x.StudentId == studentId &&
                (x.Status == "PENDING" ||
                 x.Status == "PROPOSED" ||
                 x.Status == "CONFIRMED"),
                cancellationToken);

        if (active)
            throw new ConflictException(
                "APPOINTMENT_ALREADY_ACTIVE",
                "Bạn đã có lịch hẹn đang xử lý với phòng này.");

        await EnsureSlotAvailableAsync(
            roomId, studentId, at, null, cancellationToken);

        var now = DateTime.UtcNow;

        var appt = new ViewingAppointment
        {
            RoomId = roomId,
            StudentId = studentId,
            ScheduledAt = at,
            Note = note,
            Status = "PENDING",
            CreatedAt = now,
            UpdatedAt = now
        };

        _db.ViewingAppointments.Add(appt);
        await _db.SaveChangesAsync(cancellationToken);

        AddHistory(
            appt.AppointmentId,
            userId,
            "CREATED",
            null,
            "PENDING",
            null,
            at,
            note);

        await _db.SaveChangesAsync(cancellationToken);
        await tx.CommitAsync(cancellationToken);

        return await GetDetailAsync(
            userId, appt.AppointmentId, false, cancellationToken);
    }

    // ==================================================
    // STUDENT / LANDLORD - GET LIST
    // ==================================================

    public async Task<PagedResponse<ViewingAppointmentResponse>>
        GetListAsync(
            long userId,
            bool asLandlord,
            int page,
            int pageSize,
            CancellationToken cancellationToken = default)
    {
        ValidatePage(page, pageSize);

        var actorId = await GetProfileIdAsync(
            userId, asLandlord, cancellationToken);

        var query = _db.ViewingAppointments
            .AsNoTracking()
            .Include(x => x.Room)
                .ThenInclude(x => x.Property)
            .Include(x => x.Student)
                .ThenInclude(x => x.User)
            .AsSplitQuery()
            .AsQueryable();

        query = asLandlord
            ? query.Where(x =>
                x.Room.Property.LandlordId == actorId)
            : query.Where(x =>
                x.StudentId == actorId);

        var total = await query.CountAsync(cancellationToken);

        var rows = await query
            .OrderByDescending(x => x.CreatedAt)
            .ThenByDescending(x => x.AppointmentId)
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .ToListAsync(cancellationToken);

        var totalPages = (int)Math.Ceiling(
            total / (double)pageSize);

        return new PagedResponse<ViewingAppointmentResponse>
        {
            Items = rows.Select(ToResponse).ToList(),
            TotalCount = total,
            Page = page,
            PageSize = pageSize,
            TotalPages = totalPages,
            HasNextPage = page < totalPages,
            HasPreviousPage = page > 1
        };
    }

    // ==================================================
    // GET DETAIL
    // ==================================================

    public async Task<ViewingAppointmentResponse> GetDetailAsync(
        long userId,
        long appointmentId,
        bool asLandlord,
        CancellationToken cancellationToken = default)
    {
        var appt = await FindAccessibleAsync(
            userId, appointmentId, asLandlord,
            false, cancellationToken);

        return ToResponse(appt);
    }

    // ==================================================
    // GET HISTORY
    // ==================================================

    public async Task<IReadOnlyList<ViewingAppointmentHistoryResponse>>
        GetHistoryAsync(
            long userId,
            long appointmentId,
            bool asLandlord,
            CancellationToken cancellationToken = default)
    {
        await FindAccessibleAsync(
            userId, appointmentId, asLandlord,
            false, cancellationToken);

        var rows = await _db.Set<ViewingAppointmentHistory>()
            .AsNoTracking()
            .Where(x => x.AppointmentId == appointmentId)
            .OrderBy(x => x.CreatedAt)
            .ThenBy(x => x.AppointmentHistoryId)
            .ToListAsync(cancellationToken);

        return rows.Select(x => new ViewingAppointmentHistoryResponse
        {
            AppointmentHistoryId = x.AppointmentHistoryId,
            AppointmentId = x.AppointmentId,
            ActorUserId = x.ActorUserId,
            Action = x.Action,
            OldStatus = x.OldStatus,
            NewStatus = x.NewStatus,
            OldScheduledAt = AsUtc(x.OldScheduledAt),
            NewScheduledAt = AsUtc(x.NewScheduledAt),
            Note = x.Note,
            CreatedAt = AsUtc(x.CreatedAt)
        }).ToList();
    }

    // ==================================================
    // STUDENT / LANDLORD - UPDATE APPOINTMENT
    // ==================================================

    public async Task<ViewingAppointmentResponse> UpdateAsync(
        long userId,
        long appointmentId,
        bool asLandlord,
        string action,
        DateTimeOffset? proposedAt,
        string? note,
        CancellationToken cancellationToken = default)
    {
        var normalizedNote = ValidateNote(note);

        await using var tx = await _db.Database
            .BeginTransactionAsync(cancellationToken);

        var appt = await FindAccessibleAsync(
            userId, appointmentId, asLandlord,
            true, cancellationToken);

        await LockActorsAsync(
            appt.StudentId, appt.RoomId, cancellationToken);

        await _db.Entry(appt).ReloadAsync(cancellationToken);

        var oldStatus = appt.Status;
        var oldAt = appt.ScheduledAt;
        var now = DateTime.UtcNow;

        DateTime? newEventAt = appt.ScheduledAt;

        // STUDENT - CANCEL
        if (!asLandlord && action == "CANCEL")
        {
            RequireStatus(
                appt, "PENDING", "PROPOSED", "CONFIRMED");

            var effectiveTime = appt.Status == "PROPOSED"
                ? appt.ProposedAt ?? appt.ScheduledAt
                : appt.ScheduledAt;

            if (effectiveTime <= now)
                throw new ConflictException(
                    "APPOINTMENT_ALREADY_STARTED",
                    "Không thể hủy lịch đã đến giờ.");

            appt.Status = "CANCELLED";
            appt.ProposedAt = null;
        }

        // STUDENT - ACCEPT PROPOSAL
        else if (!asLandlord && action == "ACCEPT_PROPOSAL")
        {
            RequireStatus(appt, "PROPOSED");

            if (!appt.ProposedAt.HasValue)
                throw new ConflictException(
                    "PROPOSAL_NOT_FOUND",
                    "Chưa có đề xuất giờ.");

            var at = ValidateTime(new DateTimeOffset(
                DateTime.SpecifyKind(
                    appt.ProposedAt.Value, DateTimeKind.Utc)));

            await RequireAvailableRoomAsync(
                appt.RoomId, cancellationToken);

            await EnsureSlotAvailableAsync(
                appt.RoomId,
                appt.StudentId,
                at,
                appt.AppointmentId,
                cancellationToken);

            appt.ScheduledAt = at;
            appt.ProposedAt = null;
            appt.Status = "CONFIRMED";
            newEventAt = at;
        }

        // LANDLORD - CONFIRM
        else if (asLandlord && action == "CONFIRM")
        {
            RequireStatus(appt, "PENDING");

            ValidateTime(new DateTimeOffset(
                DateTime.SpecifyKind(
                    appt.ScheduledAt, DateTimeKind.Utc)));

            await RequireAvailableRoomAsync(
                appt.RoomId, cancellationToken);

            await EnsureSlotAvailableAsync(
                appt.RoomId,
                appt.StudentId,
                appt.ScheduledAt,
                appt.AppointmentId,
                cancellationToken);

            appt.Status = "CONFIRMED";
            appt.LandlordNote = normalizedNote;
            appt.RespondedAt = now;
        }

        // LANDLORD - PROPOSE NEW TIME
        else if (asLandlord && action == "PROPOSE")
        {
            RequireStatus(appt, "PENDING");

            if (!proposedAt.HasValue)
                throw new BadRequestException(
                    "PROPOSED_AT_REQUIRED",
                    "Thiếu thời gian đề xuất.");

            var at = ValidateTime(proposedAt.Value);

            if (at == appt.ScheduledAt)
                throw new BadRequestException(
                    "SAME_PROPOSED_TIME",
                    "Thời gian mới phải khác giờ cũ.");

            await RequireAvailableRoomAsync(
                appt.RoomId, cancellationToken);

            await EnsureSlotAvailableAsync(
                appt.RoomId,
                appt.StudentId,
                at,
                appt.AppointmentId,
                cancellationToken);

            appt.ProposedAt = at;
            appt.Status = "PROPOSED";
            appt.LandlordNote = normalizedNote;
            appt.RespondedAt = now;
            newEventAt = at;
        }

        // LANDLORD - REJECT
        else if (asLandlord && action == "REJECT")
        {
            RequireStatus(appt, "PENDING", "PROPOSED");

            if (string.IsNullOrWhiteSpace(normalizedNote))
                throw new BadRequestException(
                    "REJECTION_REASON_REQUIRED",
                    "Vui lòng nhập lý do từ chối.");

            appt.ProposedAt = null;
            appt.Status = "REJECTED";
            appt.LandlordNote = normalizedNote;
            appt.RespondedAt = now;
        }

        // LANDLORD - COMPLETE
        else if (asLandlord && action == "COMPLETE")
        {
            RequireStatus(appt, "CONFIRMED");

            if (now < appt.ScheduledAt.AddMinutes(60))
                throw new ConflictException(
                    "APPOINTMENT_NOT_FINISHED",
                    "Chỉ có thể hoàn thành sau khi kết thúc buổi xem 60 phút.");

            appt.Status = "COMPLETED";
            appt.LandlordNote =
                normalizedNote ?? appt.LandlordNote;
            appt.RespondedAt = now;
        }

        else
        {
            throw new BadRequestException(
                "INVALID_ACTION",
                "Thao tác không hợp lệ.");
        }

        appt.UpdatedAt = now;

        AddHistory(
            appt.AppointmentId,
            userId,
            action,
            oldStatus,
            appt.Status,
            oldAt,
            newEventAt,
            normalizedNote);

        try
        {
            await _db.SaveChangesAsync(cancellationToken);
            await tx.CommitAsync(cancellationToken);
        }
        catch (DbUpdateConcurrencyException)
        {
            throw new ConflictException(
                "APPOINTMENT_CHANGED",
                "Lịch đã được người khác cập nhật. Vui lòng tải lại.");
        }
        catch (SqlException ex) when (ex.Number == 1205)
        {
            throw new ConflictException(
                "APPOINTMENT_BUSY",
                "Lịch đang được xử lý. Vui lòng thử lại.");
        }

        return await GetDetailAsync(
            userId, appointmentId, asLandlord, cancellationToken);
    }

    // ==================================================
    // HELPERS - PERMISSIONS
    // ==================================================

    private async Task<ViewingAppointment> FindAccessibleAsync(
        long userId,
        long appointmentId,
        bool asLandlord,
        bool tracked,
        CancellationToken ct)
    {
        var actorId = await GetProfileIdAsync(
            userId, asLandlord, ct);

        var query = _db.ViewingAppointments
            .Include(x => x.Room)
                .ThenInclude(x => x.Property)
            .Include(x => x.Student)
                .ThenInclude(x => x.User)
            .AsQueryable();

        if (!tracked)
            query = query.AsNoTracking();

        query = asLandlord
            ? query.Where(x =>
                x.Room.Property.LandlordId == actorId)
            : query.Where(x =>
                x.StudentId == actorId);

        var result = await query.SingleOrDefaultAsync(
            x => x.AppointmentId == appointmentId, ct);

        if (result is null)
            throw new NotFoundException(
                "APPOINTMENT_NOT_FOUND",
                "Không tìm thấy lịch hẹn.");

        return result;
    }

    private async Task<long> GetProfileIdAsync(
        long userId,
        bool asLandlord,
        CancellationToken ct)
    {
        long? id = asLandlord
            ? await _db.LandlordProfiles
                .AsNoTracking()
                .Where(x => x.UserId == userId)
                .Select(x => (long?)x.LandlordId)
                .SingleOrDefaultAsync(ct)
            : await _db.StudentProfiles
                .AsNoTracking()
                .Where(x => x.UserId == userId)
                .Select(x => (long?)x.StudentId)
                .SingleOrDefaultAsync(ct);

        if (!id.HasValue)
            throw new ForbiddenException(
                "PROFILE_REQUIRED",
                "Tài khoản chưa có hồ sơ tương ứng với vai trò.");

        return id.Value;
    }

    private async Task RequireAvailableRoomAsync(
        long roomId,
        CancellationToken ct)
    {
        var room = await _db.Rooms
            .AsNoTracking()
            .Where(x =>
                x.RoomId == roomId &&
                x.ListingStatus == "PUBLISHED" &&
                x.Property.Status == "ACTIVE")
            .Select(x => new { x.AvailabilityStatus })
            .SingleOrDefaultAsync(ct);

        if (room is null)
            throw new NotFoundException(
                "ROOM_NOT_FOUND",
                "Không tìm thấy phòng công khai.");

        if (room.AvailabilityStatus != "AVAILABLE")
            throw new ConflictException(
                "ROOM_NOT_AVAILABLE",
                "Phòng hiện không còn cho thuê.");
    }

    // ==================================================
    // HELPERS - CONFLICT CHECK
    // ==================================================

    private async Task EnsureSlotAvailableAsync(
        long roomId,
        long studentId,
        DateTime at,
        long? excludeId,
        CancellationToken ct)
    {
        var from = at.AddMinutes(-60);
        var to = at.AddMinutes(60);

        var overlaps = await _db.ViewingAppointments
            .AsNoTracking()
            .Where(x =>
                (!excludeId.HasValue ||
                    x.AppointmentId != excludeId.Value) &&
                (x.RoomId == roomId ||
                    x.StudentId == studentId) &&
                (x.Status == "PENDING" ||
                 x.Status == "PROPOSED" ||
                 x.Status == "CONFIRMED"))
            .Where(x =>
                x.Status == "PROPOSED"
                    ? x.ProposedAt > from && x.ProposedAt < to
                    : x.ScheduledAt > from && x.ScheduledAt < to)
            .Select(x => new { x.RoomId, x.StudentId })
            .ToListAsync(ct);

        if (overlaps.Any(x => x.RoomId == roomId))
            throw new ConflictException(
                "ROOM_SLOT_UNAVAILABLE",
                "Giờ này đã có lịch xem phòng.");

        if (overlaps.Any(x => x.StudentId == studentId))
            throw new ConflictException(
                "STUDENT_SCHEDULE_CONFLICT",
                "Bạn đã có lịch xem phòng khác trùng thời gian.");
    }

    // ==================================================
    // HELPERS - SQL APPLICATION LOCK
    // ==================================================

    private async Task LockActorsAsync(
        long studentId,
        long roomId,
        CancellationToken ct)
    {
        await LockAsync(
            $"Roomify:Viewing:Student:{studentId}", ct);

        await LockAsync(
            $"Roomify:Viewing:Room:{roomId}", ct);
    }

    private async Task LockAsync(
        string resource,
        CancellationToken ct)
    {
        var connection = _db.Database.GetDbConnection();

        await using var cmd = connection.CreateCommand();

        cmd.Transaction = _db.Database.CurrentTransaction?
            .GetDbTransaction();

        cmd.CommandText = @"
DECLARE @result int;
EXEC @result = sys.sp_getapplock
    @Resource = @resource,
    @LockMode = 'Exclusive',
    @LockOwner = 'Transaction',
    @LockTimeout = 5000;
SELECT @result;";

        var parameter = cmd.CreateParameter();

        parameter.ParameterName = "@resource";
        parameter.Value = resource;

        cmd.Parameters.Add(parameter);

        var result = Convert.ToInt32(
            await cmd.ExecuteScalarAsync(ct));

        if (result < 0)
            throw new ConflictException(
                "APPOINTMENT_BUSY",
                "Lịch đang được xử lý. Vui lòng thử lại.");
    }

    // ==================================================
    // HELPERS - HISTORY
    // ==================================================

    private void AddHistory(
        long appointmentId,
        long actorUserId,
        string action,
        string? oldStatus,
        string newStatus,
        DateTime? oldAt,
        DateTime? newAt,
        string? note)
    {
        _db.Set<ViewingAppointmentHistory>().Add(
            new ViewingAppointmentHistory
            {
                AppointmentId = appointmentId,
                ActorUserId = actorUserId,
                Action = action,
                OldStatus = oldStatus,
                NewStatus = newStatus,
                OldScheduledAt = oldAt,
                NewScheduledAt = newAt,
                Note = note,
                CreatedAt = DateTime.UtcNow
            });
    }

    // ==================================================
    // HELPERS - VALIDATION
    // ==================================================

    private static DateTime ValidateTime(DateTimeOffset value)
    {
        if (value == default)
            throw new BadRequestException(
                "INVALID_APPOINTMENT_TIME",
                "Vui lòng chọn ngày giờ.");

        var utc = value.UtcDateTime;
        var now = DateTime.UtcNow;

        if (utc < now.AddHours(2) || utc > now.AddDays(30))
            throw new BadRequestException(
                "INVALID_APPOINTMENT_TIME",
                "Lịch phải trước ít nhất 2 giờ và trong 30 ngày tới.");

        var local = utc.AddHours(7);

        if (local.Hour < 8 ||
            local.Hour > 19 ||
            (local.Hour == 19 && local.Minute != 0) ||
            (local.Minute != 0 && local.Minute != 30) ||
            local.Second != 0 ||
            local.Millisecond != 0)
            throw new BadRequestException(
                "INVALID_APPOINTMENT_SLOT",
                "Giờ bắt đầu từ 08:00 đến 19:00, cách nhau 30 phút.");

        return utc;
    }

    private static string? ValidateNote(string? note)
    {
        var result = note?.Trim();

        if (result?.Length > 1000)
            throw new BadRequestException(
                "NOTE_TOO_LONG",
                "Ghi chú tối đa 1000 ký tự.");

        return string.IsNullOrWhiteSpace(result)
            ? null : result;
    }

    private static void ValidatePage(int page, int pageSize)
    {
        if (page < 1 ||
            page > 100000 ||
            pageSize < 1 ||
            pageSize > 50)
            throw new BadRequestException(
                "INVALID_PAGINATION",
                "Page phải >= 1; PageSize từ 1 đến 50.");
    }

    private static void RequireStatus(
        ViewingAppointment a,
        params string[] statuses)
    {
        if (!statuses.Contains(a.Status))
            throw new ConflictException(
                "INVALID_APPOINTMENT_STATUS",
                "Trạng thái lịch hẹn không cho phép thao tác này.");
    }

    // ==================================================
    // HELPERS - RESPONSE
    // ==================================================

    private static DateTime AsUtc(DateTime date) =>
        DateTime.SpecifyKind(date, DateTimeKind.Utc);

    private static DateTime? AsUtc(DateTime? date) =>
        date.HasValue ? AsUtc(date.Value) : null;

    private static ViewingAppointmentResponse ToResponse(
        ViewingAppointment a) => new()
        {
            AppointmentId = a.AppointmentId,
            RoomId = a.RoomId,
            RoomTitle = a.Room.Title,
            PropertyName = a.Room.Property.PropertyName,
            StudentId = a.StudentId,
            StudentName = a.Student.User.FullName,
            StudentPhone = a.Student.User.Phone,
            ScheduledAt = AsUtc(a.ScheduledAt),
            ProposedAt = AsUtc(a.ProposedAt),
            Status = a.Status,
            Note = a.Note,
            LandlordNote = a.LandlordNote,
            RespondedAt = AsUtc(a.RespondedAt),
            CreatedAt = AsUtc(a.CreatedAt),
            UpdatedAt = AsUtc(a.UpdatedAt)
        };
}
