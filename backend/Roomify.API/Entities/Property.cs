using System;
using System.Collections.Generic;

namespace Roomify.API.Entities;

public partial class Property
{
    public long PropertyId { get; set; }

    public long LandlordId { get; set; }

    public int PropertyTypeId { get; set; }

    public string PropertyName { get; set; } = null!;

    public string? Description { get; set; }

    public int ProvinceId { get; set; }

    public int WardId { get; set; }

    public string AddressLine { get; set; } = null!;

    public decimal? Latitude { get; set; }

    public decimal? Longitude { get; set; }

    public string Status { get; set; } = null!;

    public DateTime CreatedAt { get; set; }

    public DateTime UpdatedAt { get; set; }

    public virtual LandlordProfile Landlord { get; set; } = null!;

    public virtual PropertyType PropertyType { get; set; } = null!;

    public virtual Province Province { get; set; } = null!;

    public virtual ICollection<Room> Rooms { get; set; } = new List<Room>();

    public virtual Ward Ward { get; set; } = null!;
}
