using System.ComponentModel.DataAnnotations;

namespace Roomify.API.DTOs.Landlord;

public class UpdatePropertyStatusRequest
{
    [Required]
    public string Status { get; set; } = string.Empty;
}