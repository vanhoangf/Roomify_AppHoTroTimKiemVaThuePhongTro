using System;
using System.Collections.Generic;

namespace Roomify.API.Entities;

public partial class SavedRoom
{
    public long StudentId { get; set; }

    public long RoomId { get; set; }

    public DateTime SavedAt { get; set; }

    public virtual Room Room { get; set; } = null!;

    public virtual StudentProfile Student { get; set; } = null!;
}
