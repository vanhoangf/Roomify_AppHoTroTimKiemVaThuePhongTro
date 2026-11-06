using System.ComponentModel.DataAnnotations;

namespace Roomify.API.DTOs.Rentals;

public class RentalRequestDecisionRequest
{
    [MaxLength(1500)]
    public string? Note { get; set; }
}