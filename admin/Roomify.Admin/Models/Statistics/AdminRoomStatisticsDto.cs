namespace Roomify.Admin.Models.Statistics;

public class AdminRoomStatisticsDto
{
    public int TotalRooms { get; set; }

    public int PendingRooms { get; set; }

    public int PublishedRooms { get; set; }

    public int RejectedRooms { get; set; }

    public int CreatedInPeriod { get; set; }

    public List<AdminStatisticsCountPointDto> RoomsByDate{ get; set; } = [];
}