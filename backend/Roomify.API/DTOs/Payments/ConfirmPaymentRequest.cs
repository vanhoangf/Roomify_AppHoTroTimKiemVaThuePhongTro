using System.ComponentModel.DataAnnotations;

namespace Roomify.API.DTOs.Payments;

public class ConfirmPaymentRequest
{
    [MaxLength(1000)]
    public string? Note { get; set; }
}