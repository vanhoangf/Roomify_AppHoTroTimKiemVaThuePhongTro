using Roomify.Admin.Models.Common;
using Roomify.Admin.Models.Dashboard;

namespace Roomify.Admin.Services.Dashboard;

public interface IAdminDashboardApiService
{
    Task<ApiResult<AdminDashboardDto>>
        GetDashboardAsync(
            CancellationToken cancellationToken = default);
}