using Roomify.Admin.Models.Common;
using Roomify.Admin.Models.Reports;

namespace Roomify.Admin.Services.Reports;

public interface IAdminReportApiService
{
    Task<ApiResult<AdminReportListDto>>
        GetReportsAsync(
            string? status = null,
            string? search = null,
            int page = 1,
            int pageSize = 10,
            CancellationToken cancellationToken = default);


    Task<ApiResult<AdminReportDto>>
        GetReportByIdAsync(
            long reportId,
            CancellationToken cancellationToken = default);


    Task<ApiResult<AdminReportDto>>
        ResolveReportAsync(
            long reportId,
            string adminNote,
            CancellationToken cancellationToken = default);


    Task<ApiResult<AdminReportDto>>
        RejectReportAsync(
            long reportId,
            string adminNote,
            CancellationToken cancellationToken = default);
}