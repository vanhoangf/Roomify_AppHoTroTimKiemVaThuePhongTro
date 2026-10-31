namespace Roomify.API.DTOs.Catalog;

public class WardResponse
{
    public int WardId { get; set; }

    public int ProvinceId { get; set; }

    public string? WardCode { get; set; }

    public string WardName { get; set; } = string.Empty;

    public string WardType { get; set; } = string.Empty;
}