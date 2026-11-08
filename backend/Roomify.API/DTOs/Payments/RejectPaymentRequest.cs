using System.ComponentModel.DataAnnotations;

namespace Roomify.API.DTOs.Payments;

public class RejectPaymentRequest
{
    [Required]
    [MaxLength(1000)]
    public string Note { get; set; } = string.Empty;
}