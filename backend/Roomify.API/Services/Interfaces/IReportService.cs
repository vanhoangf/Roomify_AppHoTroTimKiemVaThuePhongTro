using Roomify.API.DTOs.Reports;

namespace Roomify.API.Services.Interfaces;

public interface IReportService
{
    // Báo cáo một phòng
    Task<ReportResponse> CreateAsync(
        long userId,
        long roomId,
        CreateReportRequest request);

    // Danh sách report do chính user hiện tại tạo
    Task<ReportListResponse> GetMyReportsAsync(
        long userId,
        string? status,
        int page,
        int pageSize);

    // Xem chi tiết report của chính mình
    Task<ReportResponse> GetMyReportByIdAsync(
        long userId,
        long reportId);
}