namespace Roomify.Admin.Models.Catalog;

public class ProvinceDto
{
    public int ProvinceId { get; set; }

    public string? ProvinceCode { get; set; }

    public string ProvinceName { get; set; }
        = string.Empty;

    public string ProvinceType { get; set; }
        = string.Empty;
}