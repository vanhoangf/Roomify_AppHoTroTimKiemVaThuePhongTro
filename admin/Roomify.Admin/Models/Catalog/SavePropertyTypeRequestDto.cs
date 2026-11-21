using System.ComponentModel.DataAnnotations;

namespace Roomify.Admin.Models.Catalog;

public class SavePropertyTypeRequestDto
{
    [Required]
    [MaxLength(100)]
    public string TypeName { get; set; }
        = string.Empty;

    public bool IsActive { get; set; } = true;
}