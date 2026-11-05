
namespace Roomify.API.DTOs.Student;

public class SavedRoomResponse
{
    public long RoomId { get; set; }

    public long PropertyId { get; set; }

    public string PropertyName { get; set; }
        = string.Empty;

    public string Title { get; set; }
        = string.Empty;

    public decimal RentPrice { get; set; }

    public decimal AreaM2 { get; set; }

    public string AvailabilityStatus { get; set; }
        = string.Empty;

    public string AddressLine { get; set; }
        = string.Empty;

    public string ProvinceName { get; set; }
        = string.Empty;

    public string WardName { get; set; }
        = string.Empty;

    public string? ThumbnailUrl { get; set; }

    public DateTime SavedAt { get; set; }
}
