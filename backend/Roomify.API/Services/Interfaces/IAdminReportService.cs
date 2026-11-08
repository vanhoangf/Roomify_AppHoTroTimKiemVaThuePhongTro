using Roomify.API.DTOs.Admin.Reports;

namespace Roomify.API.Services.Interfaces;

public interface IAdminReportService
{
    Task<AdminReportListResponse> GetReportsAsync(
        string? status,
        string? search,
        int page,
        int pageSize);

    Task<AdminReportResponse> GetReportByIdAsync(
        long reportId);

    Task<AdminReportResponse> ResolveAsync(
        long adminUserId,
        long reportId,
        AdminReportDecisionRequest request);

    Task<AdminReportResponse> RejectAsync(
        long adminUserId,
        long reportId,
        AdminReportDecisionRequest request);
}