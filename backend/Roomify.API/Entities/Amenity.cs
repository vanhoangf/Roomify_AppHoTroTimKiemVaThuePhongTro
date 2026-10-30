using System;
using System.Collections.Generic;

namespace Roomify.API.Entities;

public partial class Amenity
{
    public int AmenityId { get; set; }

    public string AmenityName { get; set; } = null!;

    public string? IconUrl { get; set; }

    public bool IsActive { get; set; }

    public virtual ICollection<Room> Rooms { get; set; } = new List<Room>();
}
