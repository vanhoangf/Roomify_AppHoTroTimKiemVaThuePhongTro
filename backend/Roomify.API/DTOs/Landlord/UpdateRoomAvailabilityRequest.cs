using System.ComponentModel.DataAnnotations;

namespace Roomify.API.DTOs.Landlord;

public class UpdateRoomAvailabilityRequest
{
    [Required]
    public string AvailabilityStatus { get; set; } = string.Empty;
}