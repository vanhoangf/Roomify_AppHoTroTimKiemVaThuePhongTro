namespace Roomify.Admin.Models.Statistics;

public class AdminUserStatisticsDto
{
    public int TotalUsers { get; set; }

    public int Students { get; set; }

    public int Landlords { get; set; }

    public int NewUsersInPeriod { get; set; }

    public List<AdminStatisticsCountPointDto> NewUsersByDate{ get; set; } = [];
}