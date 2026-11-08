using System.ComponentModel.DataAnnotations;

namespace Roomify.API.DTOs.Admin.Catalog;

public class SaveUniversityRequest
{
    [Required]
    [MaxLength(255)]
    public string UniversityName { get; set; } = string.Empty;

    public int? ProvinceId { get; set; }

    public int? WardId { get; set; }

    [MaxLength(500)]
    public string? AddressLine { get; set; }

    [Range(-90, 90)]
    public decimal? Latitude { get; set; }

    [Range(-180, 180)]
    public decimal? Longitude { get; set; }

    public bool IsActive { get; set; } = true;
}