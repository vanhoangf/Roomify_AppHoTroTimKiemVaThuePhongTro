using System;
using System.Collections.Generic;

namespace Roomify.API.Entities;

public partial class PropertyType
{
    public int PropertyTypeId { get; set; }

    public string TypeName { get; set; } = null!;

    public bool IsActive { get; set; }

    public virtual ICollection<Property> Properties { get; set; } = new List<Property>();
}
