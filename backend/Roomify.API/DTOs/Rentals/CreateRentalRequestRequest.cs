using System.ComponentModel.DataAnnotations;

namespace Roomify.API.DTOs.Rentals;

public class CreateRentalRequestRequest
{
    [Required]
    public DateOnly RequestedMoveInDate { get; set; }

    [Range(1, 120)]
    public int? DurationMonths { get; set; }

    [MaxLength(1500)]
    public string? Note { get; set; }
}