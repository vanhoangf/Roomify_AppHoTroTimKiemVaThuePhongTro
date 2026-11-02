namespace Roomify.API.DTOs.Landlord;

public class RoomImageResponse
{
    public long ImageId { get; set; }

    public long RoomId { get; set; }

    public string ImageUrl { get; set; }
        = string.Empty;

    public bool IsPrimary { get; set; }

    public int SortOrder { get; set; }

    public DateTime CreatedAt { get; set; }
}