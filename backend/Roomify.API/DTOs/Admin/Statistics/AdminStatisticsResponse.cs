namespace Roomify.API.DTOs.Admin.Statistics;

public class AdminStatisticsResponse
{
    public DateOnly FromDate { get; set; }

    public DateOnly ToDate { get; set; }

    public int PeriodDays { get; set; }

    public AdminStatisticsOverviewResponse Overview { get; set; } = new();

    public AdminRevenueStatisticsResponse Revenue { get; set; } = new();

    public AdminContractStatisticsResponse Contracts { get; set; } = new();

    public AdminUserStatisticsResponse Users { get; set; } = new();

    public AdminRoomStatisticsResponse Rooms { get; set; } = new();
}