using System;
using System.Collections.Generic;

namespace Roomify.API.Entities;

public partial class Report
{
    public long ReportId { get; set; }

    public long ReporterUserId { get; set; }

    public long RoomId { get; set; }

    public string Reason { get; set; } = null!;

    public string? Description { get; set; }

    public string Status { get; set; } = null!;

    public string? AdminNote { get; set; }

    public long? ResolvedByUserId { get; set; }

    public DateTime? ResolvedAt { get; set; }

    public DateTime CreatedAt { get; set; }

    public virtual User ReporterUser { get; set; } = null!;

    public virtual User? ResolvedByUser { get; set; }

    public virtual Room Room { get; set; } = null!;
}
