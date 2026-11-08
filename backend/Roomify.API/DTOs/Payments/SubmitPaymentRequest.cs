using System.ComponentModel.DataAnnotations;

namespace Roomify.API.DTOs.Payments;

public class SubmitPaymentRequest
{
    [Required]
    [MaxLength(30)]
    public string PaymentMethod { get; set; } = string.Empty;

    [MaxLength(150)]
    public string? TransactionReference { get; set; }

    [MaxLength(1000)]
    public string? ProofImageUrl { get; set; }

    [MaxLength(1000)]
    public string? Note { get; set; }
}