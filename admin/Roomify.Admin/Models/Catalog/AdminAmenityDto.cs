namespace Roomify.Admin.Models.Catalog;

public class AdminAmenityDto
{
    public int AmenityId { get; set; }

    public string AmenityName { get; set; }
        = string.Empty;

    public string? IconUrl { get; set; }

    public bool IsActive { get; set; }
}