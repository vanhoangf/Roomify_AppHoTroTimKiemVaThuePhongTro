namespace Roomify.Admin.Models.Statistics;

public class AdminStatisticsDto
{
    public DateOnly FromDate { get; set; }

    public DateOnly ToDate { get; set; }

    public int PeriodDays { get; set; }

    public AdminStatisticsOverviewDto Overview { get; set; } = new();

    public AdminRevenueStatisticsDto Revenue { get; set; } = new();

    public AdminContractStatisticsDto Contracts { get; set; } = new();

    public AdminUserStatisticsDto Users { get; set; } = new();

    public AdminRoomStatisticsDto Rooms { get; set; } = new();
}