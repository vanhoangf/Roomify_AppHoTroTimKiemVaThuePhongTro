using System;
using System.Collections.Generic;

namespace Roomify.API.Entities;

public partial class Province
{
    public int ProvinceId { get; set; }

    public string? ProvinceCode { get; set; }

    public string ProvinceName { get; set; } = null!;

    public string ProvinceType { get; set; } = null!;

    public bool IsActive { get; set; }

    public virtual ICollection<Property> Properties { get; set; } = new List<Property>();

    public virtual ICollection<University> Universities { get; set; } = new List<University>();

    public virtual ICollection<Ward> Wards { get; set; } = new List<Ward>();
}
