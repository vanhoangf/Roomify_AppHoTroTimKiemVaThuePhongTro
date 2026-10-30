using System;
using System.Collections.Generic;

namespace Roomify.API.Entities;

public partial class Notification
{
    public long NotificationId { get; set; }

    public long UserId { get; set; }

    public string? NotificationType { get; set; }

    public string Title { get; set; } = null!;

    public string Message { get; set; } = null!;

    public string? RelatedEntityType { get; set; }

    public long? RelatedEntityId { get; set; }

    public bool IsRead { get; set; }

    public DateTime? ReadAt { get; set; }

    public DateTime CreatedAt { get; set; }

    public virtual User User { get; set; } = null!;
}
