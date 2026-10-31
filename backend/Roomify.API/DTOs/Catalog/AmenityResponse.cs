namespace Roomify.API.DTOs.Catalog;

public class AmenityResponse
{
    public int AmenityId { get; set; }

    public string AmenityName { get; set; } = string.Empty;

    public string? IconUrl { get; set; }
}