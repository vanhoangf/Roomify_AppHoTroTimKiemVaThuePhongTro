using System.ComponentModel.DataAnnotations;

namespace Roomify.API.DTOs.Payments;

public class CreatePaymentRequest
{
    [Required]
    public string PaymentType { get; set; } = string.Empty;

    [Range(typeof(decimal), "0.01", "9999999999999999")]
    public decimal Amount { get; set; }

    public DateOnly? DueDate { get; set; }

    [MaxLength(1000)]
    public string? Note { get; set; }
}