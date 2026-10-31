namespace Roomify.API.DTOs.Catalog;

public class UniversityResponse
{
    public long UniversityId { get; set; }

    public string UniversityName { get; set; } = string.Empty;

    public int? ProvinceId { get; set; }

    public string? ProvinceName { get; set; }

    public int? WardId { get; set; }

    public string? WardName { get; set; }

    public string? AddressLine { get; set; }

    public decimal? Latitude { get; set; }

    public decimal? Longitude { get; set; }
}