namespace Roomify.API.DTOs.Public;

public class RoomDetailResponse
{
    public long RoomId { get; set; }

    public long PropertyId { get; set; }

    public string PropertyName { get; set; }
        = string.Empty;

    public string? PropertyDescription { get; set; }

    public int PropertyTypeId { get; set; }

    public string PropertyTypeName { get; set; }
        = string.Empty;

    public string PropertyStatus { get; set; }
        = string.Empty;

    public string? RoomNumber { get; set; }

    public string Title { get; set; }
        = string.Empty;

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

    public string GenderPreference { get; set; }
        = string.Empty;

    public DateOnly? AvailableFrom { get; set; }

    public string ListingStatus { get; set; }
        = string.Empty;

    public string AvailabilityStatus { get; set; }
        = string.Empty;

    public int ProvinceId { get; set; }

    public string ProvinceName { get; set; }
        = string.Empty;

    public int WardId { get; set; }

    public string WardName { get; set; }
        = string.Empty;

    public string AddressLine { get; set; }
        = string.Empty;

    public decimal? Latitude { get; set; }

    public decimal? Longitude { get; set; }

    public bool LandlordIsVerified { get; set; }

    public int ViewCount { get; set; }

    public DateTime? PublishedAt { get; set; }

    public List<PublicRoomImageResponse> Images
    { get; set; } = [];

    public List<PublicRoomAmenityResponse> Amenities
    { get; set; } = [];
}

public class PublicRoomImageResponse
{
    public long ImageId { get; set; }

    public string ImageUrl { get; set; }
        = string.Empty;

    public bool IsPrimary { get; set; }

    public int SortOrder { get; set; }
}

public class PublicRoomAmenityResponse
{
    public int AmenityId { get; set; }

    public string AmenityName { get; set; }
        = string.Empty;

    public string? IconUrl { get; set; }
}