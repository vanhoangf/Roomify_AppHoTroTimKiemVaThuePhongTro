using System;
using System.Collections.Generic;

namespace Roomify.API.Entities;

public partial class RentalContract
{
    public long ContractId { get; set; }

    public long RentalRequestId { get; set; }

    public string ContractCode { get; set; } = null!;

    public long RoomId { get; set; }

    public DateOnly StartDate { get; set; }

    public DateOnly? EndDate { get; set; }

    public decimal MonthlyRent { get; set; }

    public decimal DepositAmount { get; set; }

    public string? Terms { get; set; }

    public string? ContractFileUrl { get; set; }

    public string Status { get; set; } = null!;

    public DateTime CreatedAt { get; set; }

    public DateTime? ActivatedAt { get; set; }

    public DateTime? CompletedAt { get; set; }

    public DateTime? CancelledAt { get; set; }

    public virtual ICollection<Payment> Payments { get; set; } = new List<Payment>();

    public virtual RentalRequest RentalRequest { get; set; } = null!;

    public virtual Review? Review { get; set; }
}
