using System;
using System.Collections.Generic;

namespace Roomify.API.Entities;

public partial class Review
{
    public long ReviewId { get; set; }

    public long ContractId { get; set; }

    public byte Rating { get; set; }

    public string? Comment { get; set; }

    public string Status { get; set; } = null!;

    public long? ModeratedByUserId { get; set; }

    public DateTime? ModeratedAt { get; set; }

    public DateTime CreatedAt { get; set; }

    public DateTime UpdatedAt { get; set; }

    public virtual RentalContract Contract { get; set; } = null!;

    public virtual User? ModeratedByUser { get; set; }
}
