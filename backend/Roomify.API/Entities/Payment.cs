using System;
using System.Collections.Generic;

namespace Roomify.API.Entities;

public partial class Payment
{
    public long PaymentId { get; set; }

    public long ContractId { get; set; }

    public string PaymentType { get; set; } = null!;

    public decimal Amount { get; set; }

    public DateOnly? DueDate { get; set; }

    public string? PaymentMethod { get; set; }

    public string Status { get; set; } = null!;

    public string? TransactionReference { get; set; }

    public string? ProofImageUrl { get; set; }

    public string? Note { get; set; }

    public long? ConfirmedByUserId { get; set; }

    public DateTime? ConfirmedAt { get; set; }

    public DateTime? PaidAt { get; set; }

    public DateTime CreatedAt { get; set; }

    public DateTime UpdatedAt { get; set; }

    public virtual User? ConfirmedByUser { get; set; }

    public virtual RentalContract Contract { get; set; } = null!;
}
