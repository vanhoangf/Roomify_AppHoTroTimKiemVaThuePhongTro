using System;
using System.Collections.Generic;

namespace Roomify.API.Entities;

public partial class University
{
    public long UniversityId { get; set; }

    public string UniversityName { get; set; } = null!;

    public int? ProvinceId { get; set; }

    public int? WardId { get; set; }

    public string? AddressLine { get; set; }

    public decimal? Latitude { get; set; }

    public decimal? Longitude { get; set; }

    public bool IsActive { get; set; }

    public virtual Province? Province { get; set; }

    public virtual ICollection<StudentProfile> StudentProfiles { get; set; } = new List<StudentProfile>();

    public virtual Ward? Ward { get; set; }
}
