using System.ComponentModel.DataAnnotations;

namespace Roomify.API.DTOs.Admin.Rooms;

public class RejectRoomRequest
{
    [Required]
    [MaxLength(1000)]
    public string Reason { get; set; } = string.Empty;
}