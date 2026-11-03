namespace Roomify.API.DTOs.Public;

public class SearchRoomsRequest
{
    public string? Keyword { get; set; }

    public int? ProvinceId { get; set; }

    public int? WardId { get; set; }

    public long? UniversityId { get; set; }

    public double? RadiusKm { get; set; }

    public int? PropertyTypeId { get; set; }

    public decimal? MinPrice { get; set; }

    public decimal? MaxPrice { get; set; }

    public decimal? MinArea { get; set; }

    public decimal? MaxArea { get; set; }

    public int? MinOccupants { get; set; }

    public string? GenderPreference { get; set; }

    public string? AvailabilityStatus { get; set; }

    public List<int> AmenityIds { get; set; } = [];

    public string? SortBy { get; set; } = "newest";

    public int Page { get; set; } = 1;

    public int PageSize { get; set; } = 10;
}