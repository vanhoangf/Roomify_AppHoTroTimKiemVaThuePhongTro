using System;
using System.Collections.Generic;

namespace Roomify.API.Entities;

public partial class RoomImage
{
    public long ImageId { get; set; }

    public long RoomId { get; set; }

    public string ImageUrl { get; set; } = null!;

    public string? PublicId { get; set; }

    public bool IsPrimary { get; set; }

    public int SortOrder { get; set; }

    public DateTime CreatedAt { get; set; }

    public virtual Room Room { get; set; } = null!;
}
