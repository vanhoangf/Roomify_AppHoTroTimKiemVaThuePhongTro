namespace Roomify.API.DTOs.Catalog;

public class ProvinceResponse
{
    public int ProvinceId { get; set; }

    public string? ProvinceCode { get; set; }

    public string ProvinceName { get; set; } = string.Empty;

    public string ProvinceType { get; set; } = string.Empty;
}