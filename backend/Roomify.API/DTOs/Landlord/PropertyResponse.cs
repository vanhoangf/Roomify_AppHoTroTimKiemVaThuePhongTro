namespace Roomify.API.DTOs.Landlord;

public class PropertyResponse
{
    public long PropertyId { get; set; }

    public int PropertyTypeId { get; set; }

    public string PropertyTypeName { get; set; } = string.Empty;

    public string PropertyName { get; set; } = string.Empty;

    public string? Description { get; set; }

    public int ProvinceId { get; set; }

    public string ProvinceName { get; set; } = string.Empty;

    public int WardId { get; set; }

    public string WardName { get; set; } = string.Empty;

    public string AddressLine { get; set; } = string.Empty;

    public decimal? Latitude { get; set; }

    public decimal? Longitude { get; set; }

    public string Status { get; set; } = string.Empty;

    public int RoomCount { get; set; }

    public DateTime CreatedAt { get; set; }

    public DateTime UpdatedAt { get; set; }
}