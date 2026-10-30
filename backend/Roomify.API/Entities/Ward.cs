using System;
using System.Collections.Generic;

namespace Roomify.API.Entities;

public partial class Ward
{
    public int WardId { get; set; }

    public int ProvinceId { get; set; }

    public string? WardCode { get; set; }

    public string WardName { get; set; } = null!;

    public string WardType { get; set; } = null!;

    public bool IsActive { get; set; }

    public virtual ICollection<Property> Properties { get; set; } = new List<Property>();

    public virtual Province Province { get; set; } = null!;

    public virtual ICollection<University> Universities { get; set; } = new List<University>();
}
