using System;
using System.Collections.Generic;

namespace Roomify.API.Entities;

public partial class RoomViewHistory
{
    public long RoomViewHistoryId { get; set; }

    public long StudentId { get; set; }

    public long RoomId { get; set; }

    public DateTime FirstViewedAt { get; set; }

    public DateTime LastViewedAt { get; set; }

    public int ViewCount { get; set; }

    public virtual Room Room { get; set; } = null!;

    public virtual StudentProfile Student { get; set; } = null!;
}
