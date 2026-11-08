using System.ComponentModel.DataAnnotations;

namespace Roomify.API.DTOs.Admin.Catalog;

public class SavePropertyTypeRequest
{
    [Required]
    [MaxLength(100)]
    public string TypeName { get; set; } = string.Empty;

    public bool IsActive { get; set; } = true;
}