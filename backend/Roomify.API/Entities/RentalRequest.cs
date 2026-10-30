using System;
using System.Collections.Generic;

namespace Roomify.API.Entities;

public partial class RentalRequest
{
    public long RentalRequestId { get; set; }

    public long RoomId { get; set; }

    public long StudentId { get; set; }

    public DateOnly RequestedMoveInDate { get; set; }

    public int? DurationMonths { get; set; }

    public string? Note { get; set; }

    public string Status { get; set; } = null!;

    public string? LandlordNote { get; set; }

    public DateTime? RespondedAt { get; set; }

    public DateTime CreatedAt { get; set; }

    public DateTime UpdatedAt { get; set; }

    public virtual ICollection<RentalContract> RentalContracts { get; set; } = new List<RentalContract>();

    public virtual Room Room { get; set; } = null!;

    public virtual StudentProfile Student { get; set; } = null!;
}
