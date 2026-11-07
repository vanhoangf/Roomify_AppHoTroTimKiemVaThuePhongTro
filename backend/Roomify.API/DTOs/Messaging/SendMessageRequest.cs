using System.ComponentModel.DataAnnotations;

namespace Roomify.API.DTOs.Messaging;

public class SendMessageRequest
{
    [Required]
    [StringLength(20)]
    public string MessageType { get; set; } = "TEXT";

    [StringLength(2000)]
    public string? Content { get; set; }

    [StringLength(1000)]
    public string? ImageUrl { get; set; }
}