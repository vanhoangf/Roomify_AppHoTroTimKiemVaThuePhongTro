namespace Roomify.API.DTOs.Admin.Rooms;

public class AdminRoomResponse
{
    public long RoomId { get; set; }

    public long PropertyId { get; set; }

    public string PropertyName { get; set; } = string.Empty;

    public long LandlordId { get; set; }

    public long LandlordUserId { get; set; }

    public string LandlordName { get; set; } = string.Empty;

    public string LandlordEmail { get; set; } = string.Empty;

    public string? RoomNumber { get; set; }

    public string Title { get; set; } = string.Empty;

    public string? Description { get; set; }

    public decimal AreaM2 { get; set; }

    public decimal RentPrice { get; set; }

    public string ListingStatus { get; set; } = string.Empty;

    public string AvailabilityStatus { get; set; } = string.Empty;

    public DateOnly? AvailableFrom { get; set; }

    public int ViewCount { get; set; }

    public long? ReviewedByUserId { get; set; }

    public string? ReviewedByUserName { get; set; }

    public DateTime? ReviewedAt { get; set; }

    public string? RejectionReason { get; set; }

    public DateTime? PublishedAt { get; set; }

    public DateTime CreatedAt { get; set; }

    public DateTime UpdatedAt { get; set; }
}