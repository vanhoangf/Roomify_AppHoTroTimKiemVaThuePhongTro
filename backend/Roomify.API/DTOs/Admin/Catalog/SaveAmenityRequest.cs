using System.ComponentModel.DataAnnotations;

namespace Roomify.API.DTOs.Admin.Catalog;

public class SaveAmenityRequest
{
    [Required]
    [MaxLength(100)]
    public string AmenityName { get; set; } = string.Empty;

    [MaxLength(500)]
    public string? IconUrl { get; set; }

    public bool IsActive { get; set; } = true;
}