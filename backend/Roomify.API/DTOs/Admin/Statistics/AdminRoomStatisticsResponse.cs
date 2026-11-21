namespace Roomify.API.DTOs.Admin.Statistics;

public class AdminRoomStatisticsResponse
{
    public int TotalRooms { get; set; }

    public int PendingRooms { get; set; }

    public int PublishedRooms { get; set; }

    public int RejectedRooms { get; set; }

    public int CreatedInPeriod { get; set; }

    public List<AdminStatisticsCountPoint> RoomsByDate { get; set; }
        = [];
}