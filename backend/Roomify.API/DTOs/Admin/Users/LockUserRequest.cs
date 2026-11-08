using System.ComponentModel.DataAnnotations;

namespace Roomify.API.DTOs.Admin.Users;

public class LockUserRequest
{
    [Required]
    [MaxLength(500)]
    public string Reason { get; set; } = string.Empty;
}