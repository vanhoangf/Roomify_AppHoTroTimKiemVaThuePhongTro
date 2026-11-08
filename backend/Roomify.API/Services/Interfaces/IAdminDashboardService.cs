using Roomify.API.DTOs.Admin.Dashboard;

namespace Roomify.API.Services.Interfaces;

public interface IAdminDashboardService
{
    Task<AdminDashboardResponse> GetDashboardAsync();
}