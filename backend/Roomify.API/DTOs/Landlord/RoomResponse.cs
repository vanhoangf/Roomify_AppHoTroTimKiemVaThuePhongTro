namespace Roomify.API.DTOs.Landlord;

public class RoomResponse
{
    public long RoomId { get; set; }

    public long PropertyId { get; set; }

    public string PropertyName { get; set; } = string.Empty;

    public string? RoomNumber { get; set; }

    public string Title { get; set; } = string.Empty;

    public string? Description { get; set; }

    public decimal AreaM2 { get; set; }

    public decimal RentPrice { get; set; }

    public decimal? DepositAmount { get; set; }

    public decimal? ElectricityPrice { get; set; }

    public decimal? WaterPrice { get; set; }

    public decimal? InternetPrice { get; set; }

    public decimal? ServiceFee { get; set; }

    public decimal? ParkingFee { get; set; }

    public int MaxOccupants { get; set; }

    public string GenderPreference { get; set; } = string.Empty;

    public DateOnly? AvailableFrom { get; set; }

    public string ListingStatus { get; set; } = string.Empty;

    public string AvailabilityStatus { get; set; } = string.Empty;

    public int ViewCount { get; set; }

    public string? RejectionReason { get; set; }

    public DateTime? PublishedAt { get; set; }

    public List<RoomAmenityResponse> Amenities { get; set; } = [];

    public DateTime CreatedAt { get; set; }

    public DateTime UpdatedAt { get; set; }
}

public class RoomAmenityResponse
{
    public int AmenityId { get; set; }

    public string AmenityName { get; set; } = string.Empty;

    public string? IconUrl { get; set; }
}