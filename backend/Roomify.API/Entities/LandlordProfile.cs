using System;
using System.Collections.Generic;

namespace Roomify.API.Entities;

public partial class LandlordProfile
{
    public long LandlordId { get; set; }

    public long UserId { get; set; }

    public string? IdentityNumber { get; set; }

    public string? IdentityDocumentUrl { get; set; }

    public bool IsVerified { get; set; }

    public long? VerifiedByUserId { get; set; }

    public DateTime? VerifiedAt { get; set; }

    public DateTime CreatedAt { get; set; }

    public DateTime UpdatedAt { get; set; }

    public virtual ICollection<Property> Properties { get; set; } = new List<Property>();

    public virtual User User { get; set; } = null!;

    public virtual User? VerifiedByUser { get; set; }
}
