using Roomify.API.DTOs.Admin.Statistics;

namespace Roomify.API.Services.Interfaces;

public interface IAdminStatisticsService
{
    Task<AdminStatisticsResponse>
        GetStatisticsAsync(
            DateOnly? fromDate,
            DateOnly? toDate);
}