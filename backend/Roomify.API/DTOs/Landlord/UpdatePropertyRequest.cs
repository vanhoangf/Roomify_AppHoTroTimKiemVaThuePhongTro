using System.ComponentModel.DataAnnotations;

namespace Roomify.API.DTOs.Landlord;

public class UpdatePropertyRequest
{
    [Required]
    public int PropertyTypeId { get; set; }

    [Required]
    [StringLength(255)]
    public string PropertyName { get; set; } = string.Empty;

    [StringLength(2000)]
    public string? Description { get; set; }

    [Required]
    public int ProvinceId { get; set; }

    [Required]
    public int WardId { get; set; }

    [Required]
    [StringLength(500)]
    public string AddressLine { get; set; } = string.Empty;

    [Range(-90, 90)]
    public decimal? Latitude { get; set; }

    [Range(-180, 180)]
    public decimal? Longitude { get; set; }
}