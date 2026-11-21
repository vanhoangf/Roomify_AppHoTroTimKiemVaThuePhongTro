namespace Roomify.API.DTOs.Admin.Statistics;

public class AdminStatisticsOverviewResponse
{
    public int TotalUsers { get; set; }

    public int TotalRooms { get; set; }

    public int TotalContracts { get; set; }

    public int TotalPayments { get; set; }

    public decimal TotalPaidAmount { get; set; }
}