namespace Roomify.Admin.Models.Catalog;

public class AdminPropertyTypeDto
{
    public int PropertyTypeId { get; set; }

    public string TypeName { get; set; }
        = string.Empty;

    public bool IsActive { get; set; }
}