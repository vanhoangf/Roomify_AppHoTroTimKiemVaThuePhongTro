namespace Roomify.API.DTOs.Admin.Catalog;

public class AdminPropertyTypeResponse
{
    public int PropertyTypeId { get; set; }

    public string TypeName { get; set; } = string.Empty;

    public bool IsActive { get; set; }
}