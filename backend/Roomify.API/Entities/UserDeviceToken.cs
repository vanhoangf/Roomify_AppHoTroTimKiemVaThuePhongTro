using System;
using System.Collections.Generic;

namespace Roomify.API.Entities;

public partial class UserDeviceToken
{
    public long DeviceTokenId { get; set; }

    public long UserId { get; set; }

    public string DeviceToken { get; set; } = null!;

    public string Platform { get; set; } = null!;

    public bool IsActive { get; set; }

    public DateTime LastSeenAt { get; set; }

    public DateTime CreatedAt { get; set; }

    public virtual User User { get; set; } = null!;
}
