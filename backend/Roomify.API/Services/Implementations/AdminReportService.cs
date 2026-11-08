using Microsoft.EntityFrameworkCore;
using Roomify.API.Data;
using Roomify.API.DTOs.Admin.Reports;
using Roomify.API.Entities;
using Roomify.API.Exceptions;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Services.Implementations;

public class AdminReportService : IAdminReportService
{
    private readonly RoomifyDbContext _db;

    private static readonly HashSet<string> ValidStatuses =
        new(StringComparer.OrdinalIgnoreCase)
        {
            "OPEN",
            "RESOLVED",
            "REJECTED"
        };

    public AdminReportService(RoomifyDbContext db)
    {
        _db = db;
    }

    // ============================================================
    // GET REPORT LIST
    // ============================================================

    public async Task<AdminReportListResponse> GetReportsAsync(
        string? status,
        string? search,
        int page,
        int pageSize)
    {
        ValidatePaging(page, pageSize);

        var normalizedStatus =
            ValidateStatusFilter(status);

        var normalizedSearch =
            NormalizeOptional(search);

        var query = _db.Reports
            .AsNoTracking()
            .AsQueryable();

        if (normalizedStatus is not null)
        {
            query = query.Where(x =>
                x.Status == normalizedStatus);
        }

        if (normalizedSearch is not null)
        {
            query = query.Where(x =>
                x.Reason.Contains(normalizedSearch) ||

                (x.Description != null &&
                 x.Description.Contains(normalizedSearch)) ||

                x.ReporterUser.FullName.Contains(
                    normalizedSearch) ||

                x.ReporterUser.Email.Contains(
                    normalizedSearch) ||

                x.Room.Title.Contains(
                    normalizedSearch));
        }

        var totalCount =
            await query.CountAsync();

        var items = await ProjectResponse(query)
            .OrderByDescending(x => x.CreatedAt)
            .ThenByDescending(x => x.ReportId)
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
    // GET REPORT DETAIL
    // ============================================================

    public async Task<AdminReportResponse> GetReportByIdAsync(
        long reportId)
    {
        var result = await ProjectResponse(
                _db.Reports
                    .AsNoTracking()
                    .Where(x =>
                        x.ReportId == reportId))
            .FirstOrDefaultAsync();

        if (result is null)
        {
            throw new NotFoundException(
                "REPORT_NOT_FOUND",
                "Không tìm thấy báo cáo.");
        }

        return result;
    }

    // ============================================================
    // RESOLVE REPORT
    // OPEN -> RESOLVED
    // ============================================================

    public async Task<AdminReportResponse> ResolveAsync(
        long adminUserId,
        long reportId,
        AdminReportDecisionRequest request)
    {
        var report = await _db.Reports
            .FirstOrDefaultAsync(x =>
                x.ReportId == reportId);

        if (report is null)
        {
            throw new NotFoundException(
                "REPORT_NOT_FOUND",
                "Không tìm thấy báo cáo.");
        }

        EnsureOpen(report);

        var adminNote =
            NormalizeOptional(request.AdminNote);

        if (adminNote is null)
        {
            throw new BadRequestException(
                "ADMIN_NOTE_REQUIRED",
                "Vui lòng nhập ghi chú xử lý báo cáo.");
        }

        var now = DateTime.UtcNow;

        report.Status =
            "RESOLVED";

        report.AdminNote =
            adminNote;

        report.ResolvedByUserId =
            adminUserId;

        report.ResolvedAt =
            now;

        await _db.SaveChangesAsync();

        return await GetReportByIdAsync(
            reportId);
    }

    // ============================================================
    // REJECT REPORT
    // OPEN -> REJECTED
    // ============================================================

    public async Task<AdminReportResponse> RejectAsync(
        long adminUserId,
        long reportId,
        AdminReportDecisionRequest request)
    {
        var report = await _db.Reports
            .FirstOrDefaultAsync(x =>
                x.ReportId == reportId);

        if (report is null)
        {
            throw new NotFoundException(
                "REPORT_NOT_FOUND",
                "Không tìm thấy báo cáo.");
        }

        EnsureOpen(report);

        var adminNote =
            NormalizeOptional(request.AdminNote);

        if (adminNote is null)
        {
            throw new BadRequestException(
                "ADMIN_NOTE_REQUIRED",
                "Vui lòng nhập ghi chú xử lý báo cáo.");
        }

        var now = DateTime.UtcNow;

        report.Status =
            "REJECTED";

        report.AdminNote =
            adminNote;

        report.ResolvedByUserId =
            adminUserId;

        report.ResolvedAt =
            now;

        await _db.SaveChangesAsync();

        return await GetReportByIdAsync(
            reportId);
    }

    // ============================================================
    // PROJECTION
    // ============================================================

    private static IQueryable<AdminReportResponse> ProjectResponse(
        IQueryable<Report> query)
    {
        return query.Select(x => new AdminReportResponse
        {
            ReportId =
                x.ReportId,

            ReporterUserId =
                x.ReporterUserId,

            ReporterName =
                x.ReporterUser.FullName,

            ReporterEmail =
                x.ReporterUser.Email,

            RoomId =
                x.RoomId,

            RoomTitle =
                x.Room.Title,

            Reason =
                x.Reason,

            Description =
                x.Description,

            Status =
                x.Status,

            AdminNote =
                x.AdminNote,

            ResolvedByUserId =
                x.ResolvedByUserId,

            ResolvedByUserName =
                x.ResolvedByUser != null
                    ? x.ResolvedByUser.FullName
                    : null,

            ResolvedAt =
                x.ResolvedAt,

            CreatedAt =
                x.CreatedAt
        });
    }

    // ============================================================
    // BUSINESS RULE
    // ============================================================

    private static void EnsureOpen(
        Report report)
    {
        if (!string.Equals(
                report.Status,
                "OPEN",
                StringComparison.OrdinalIgnoreCase))
        {
            throw new ConflictException(
                "REPORT_ALREADY_PROCESSED",
                "Chỉ có thể xử lý báo cáo đang ở trạng thái OPEN.");
        }
    }

    // ============================================================
    // STATUS FILTER
    // ============================================================

    private static string? ValidateStatusFilter(
        string? status)
    {
        var value =
            NormalizeOptional(status);

        if (value is null)
        {
            return null;
        }

        if (!ValidStatuses.Contains(value))
        {
            throw new BadRequestException(
                "INVALID_REPORT_STATUS",
                "Trạng thái báo cáo không hợp lệ.");
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
    // LIST RESPONSE
    // ============================================================

    private static AdminReportListResponse BuildListResponse(
        List<AdminReportResponse> items,
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

        return new AdminReportListResponse
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