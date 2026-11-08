using Microsoft.EntityFrameworkCore;
using Roomify.API.Data;
using Roomify.API.DTOs.Reports;
using Roomify.API.Entities;
using Roomify.API.Exceptions;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Services.Implementations;

public class ReportService : IReportService
{
    private readonly RoomifyDbContext _db;

    public ReportService(RoomifyDbContext db)
    {
        _db = db;
    }

    // =========================================================
    // CREATE REPORT
    // =========================================================

    public async Task<ReportResponse> CreateAsync(
        long userId,
        long roomId,
        CreateReportRequest request)
    {
        // -----------------------------------------------------
        // VALIDATE INPUT
        // -----------------------------------------------------

        if (string.IsNullOrWhiteSpace(request.Reason))
        {
            throw new BadRequestException(
                "REPORT_REASON_REQUIRED",
                "Vui lòng nhập lý do báo cáo.");
        }

        var reason = request.Reason.Trim();

        if (reason.Length > 255)
        {
            throw new BadRequestException(
                "REPORT_REASON_TOO_LONG",
                "Lý do báo cáo không được vượt quá 255 ký tự.");
        }

        var description = NormalizeOptional(
            request.Description);

        if (description?.Length > 2000)
        {
            throw new BadRequestException(
                "REPORT_DESCRIPTION_TOO_LONG",
                "Mô tả báo cáo không được vượt quá 2000 ký tự.");
        }

        // -----------------------------------------------------
        // ROOM MUST BE PUBLIC
        // -----------------------------------------------------

        var room = await _db.Rooms
            .AsNoTracking()
            .FirstOrDefaultAsync(x =>
                x.RoomId == roomId &&
                x.ListingStatus == "PUBLISHED");

        if (room is null)
        {
            throw new NotFoundException(
                "ROOM_NOT_FOUND",
                "Không tìm thấy phòng công khai.");
        }

        // -----------------------------------------------------
        // ONLY ONE OPEN REPORT PER USER + ROOM
        // -----------------------------------------------------

        var existingOpenReport =
            await _db.Reports
                .AsNoTracking()
                .AnyAsync(x =>
                    x.ReporterUserId == userId &&
                    x.RoomId == roomId &&
                    x.Status == "OPEN");

        if (existingOpenReport)
        {
            throw new ConflictException(
                "REPORT_ALREADY_OPEN",
                "Bạn đã có báo cáo đang chờ xử lý đối với phòng này.");
        }

        var report = new Report
        {
            ReporterUserId = userId,
            RoomId = roomId,

            Reason = reason,
            Description = description,

            Status = "OPEN",

            AdminNote = null,
            ResolvedByUserId = null,
            ResolvedAt = null,

            CreatedAt = DateTime.UtcNow
        };

        _db.Reports.Add(report);

        try
        {
            await _db.SaveChangesAsync();
        }
        catch (DbUpdateException)
        {
            // Bảo vệ thêm trong trường hợp hai request
            // được gửi gần như cùng lúc.
            var duplicate = await _db.Reports
                .AsNoTracking()
                .AnyAsync(x =>
                    x.ReporterUserId == userId &&
                    x.RoomId == roomId &&
                    x.Status == "OPEN");

            if (duplicate)
            {
                throw new ConflictException(
                    "REPORT_ALREADY_OPEN",
                    "Bạn đã có báo cáo đang chờ xử lý đối với phòng này.");
            }

            throw;
        }

        return await GetMyReportByIdAsync(
            userId,
            report.ReportId);
    }

    // =========================================================
    // GET MY REPORTS
    // =========================================================

    public async Task<ReportListResponse> GetMyReportsAsync(
        long userId,
        string? status,
        int page,
        int pageSize)
    {
        ValidatePagination(
            page,
            pageSize);

        var normalizedStatus =
            NormalizeStatus(status);

        var query = _db.Reports
            .AsNoTracking()
            .Where(x =>
                x.ReporterUserId == userId);

        if (normalizedStatus is not null)
        {
            query = query.Where(x =>
                x.Status == normalizedStatus);
        }

        var totalCount =
            await query.CountAsync();

        var items = await query
            .OrderByDescending(x => x.CreatedAt)
            .ThenByDescending(x => x.ReportId)
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .Select(x => new ReportResponse
            {
                ReportId = x.ReportId,

                ReporterUserId = x.ReporterUserId,
                ReporterName = x.ReporterUser.FullName,

                RoomId = x.RoomId,
                RoomTitle = x.Room.Title,

                Reason = x.Reason,
                Description = x.Description,

                Status = x.Status,

                AdminNote = x.AdminNote,

                ResolvedByUserId =
                    x.ResolvedByUserId,

                ResolvedAt =
                    x.ResolvedAt,

                CreatedAt =
                    x.CreatedAt
            })
            .ToListAsync();

        var totalPages =
            totalCount == 0
                ? 0
                : (int)Math.Ceiling(
                    totalCount / (double)pageSize);

        return new ReportListResponse
        {
            Items = items,

            TotalCount = totalCount,

            Page = page,
            PageSize = pageSize,
            TotalPages = totalPages,

            HasNextPage =
                page < totalPages,

            HasPreviousPage =
                page > 1
        };
    }

    // =========================================================
    // GET MY REPORT DETAIL
    // =========================================================

    public async Task<ReportResponse> GetMyReportByIdAsync(
        long userId,
        long reportId)
    {
        var report = await _db.Reports
            .AsNoTracking()
            .Where(x =>
                x.ReportId == reportId &&
                x.ReporterUserId == userId)
            .Select(x => new ReportResponse
            {
                ReportId = x.ReportId,

                ReporterUserId = x.ReporterUserId,
                ReporterName = x.ReporterUser.FullName,

                RoomId = x.RoomId,
                RoomTitle = x.Room.Title,

                Reason = x.Reason,
                Description = x.Description,

                Status = x.Status,

                AdminNote = x.AdminNote,

                ResolvedByUserId =
                    x.ResolvedByUserId,

                ResolvedAt =
                    x.ResolvedAt,

                CreatedAt =
                    x.CreatedAt
            })
            .FirstOrDefaultAsync();

        if (report is null)
        {
            throw new NotFoundException(
                "REPORT_NOT_FOUND",
                "Không tìm thấy báo cáo.");
        }

        return report;
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private static string? NormalizeOptional(
        string? value)
    {
        if (string.IsNullOrWhiteSpace(value))
        {
            return null;
        }

        return value.Trim();
    }

    private static string? NormalizeStatus(
        string? status)
    {
        if (string.IsNullOrWhiteSpace(status))
        {
            return null;
        }

        var normalized =
            status.Trim().ToUpperInvariant();

        var validStatuses = new[]
        {
            "OPEN",
            "RESOLVED",
            "REJECTED"
        };

        if (!validStatuses.Contains(normalized))
        {
            throw new BadRequestException(
                "INVALID_REPORT_STATUS",
                "Trạng thái báo cáo không hợp lệ.");
        }

        return normalized;
    }

    private static void ValidatePagination(
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
}