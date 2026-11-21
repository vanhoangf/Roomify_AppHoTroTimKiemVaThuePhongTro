using Roomify.Admin.Models.Dashboard;

namespace Roomify.Admin.Models.Rooms;

public class AdminRoomsViewModel
{
    public AdminRoomListDto? Rooms { get; set; }

    public AdminDashboardDto? Dashboard { get; set; }

    public string? ErrorMessage { get; set; }

    public string? StatisticsErrorMessage { get; set; }

    public string? Search { get; set; }

    public string? ListingStatus { get; set; }

    public int Page { get; set; } = 1;

    public int PageSize { get; set; } = 10;


    public bool HasError =>
        !string.IsNullOrWhiteSpace(ErrorMessage);


    public bool IsEmpty =>
        Rooms is not null &&
        Rooms.Items.Count == 0;


    public double PendingPercentage =>
        Percentage(
            Dashboard?.PendingRooms ?? 0,
            Dashboard?.TotalRooms ?? 0);


    public double PublishedPercentage =>
        Percentage(
            Dashboard?.PublishedRooms ?? 0,
            Dashboard?.TotalRooms ?? 0);


    public double RejectedPercentage =>
        Percentage(
            Dashboard?.RejectedRooms ?? 0,
            Dashboard?.TotalRooms ?? 0);


    private static double Percentage(
        int value,
        int total)
    {
        if (total <= 0)
        {
            return 0;
        }

        return Math.Round(
            value * 100d / total,
            1);
    }
}