namespace Roomify.API.DTOs.Admin.Statistics;

public class AdminUserStatisticsResponse
{
    public int TotalUsers { get; set; }

    public int Students { get; set; }

    public int Landlords { get; set; }

    public int NewUsersInPeriod { get; set; }

    public List<AdminStatisticsCountPoint> NewUsersByDate { get; set; } = [];
}