namespace Roomify.API.DTOs.Landlord;

public class ReorderRoomImagesRequest
{
    public List<RoomImageOrderItem> Images { get; set; }
        = [];
}

public class RoomImageOrderItem
{
    public long ImageId { get; set; }

    public int SortOrder { get; set; }
}