namespace Roomify.API.DTOs.Public;

public class RoomCardResponse
{
    public long RoomId { get; set; }

    public long PropertyId { get; set; }

    public string PropertyName { get; set; }
        = string.Empty;

    public string PropertyStatus { get; set; }
        = string.Empty;

    public int PropertyTypeId { get; set; }

    public string PropertyTypeName { get; set; }
        = string.Empty;

    public string Title { get; set; }
        = string.Empty;

    public decimal AreaM2 { get; set; }

    public decimal RentPrice { get; set; }

    public decimal? DepositAmount { get; set; }

    public int MaxOccupants { get; set; }

    public string GenderPreference { get; set; }
        = string.Empty;

    public string ListingStatus { get; set; }
        = string.Empty;

    public string AvailabilityStatus { get; set; }
        = string.Empty;

    public DateOnly? AvailableFrom { get; set; }

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

    public string? ThumbnailUrl { get; set; }

    public double? DistanceKm { get; set; }

    public DateTime? PublishedAt { get; set; }
}