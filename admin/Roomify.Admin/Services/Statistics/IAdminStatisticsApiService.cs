using Roomify.Admin.Models.Common;
using Roomify.Admin.Models.Statistics;

namespace Roomify.Admin.Services.Statistics;

public interface IAdminStatisticsApiService
{
    Task<ApiResult<AdminStatisticsDto>>
        GetStatisticsAsync(
            DateOnly? fromDate = null,
            DateOnly? toDate = null,
            CancellationToken cancellationToken = default);
}