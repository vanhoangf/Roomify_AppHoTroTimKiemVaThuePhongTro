using System;
using System.Collections.Generic;

namespace Roomify.API.Entities;

public partial class Conversation
{
    public long ConversationId { get; set; }

    public long RoomId { get; set; }

    public long StudentId { get; set; }

    public DateTime? LastMessageAt { get; set; }

    public DateTime CreatedAt { get; set; }

    public virtual ICollection<Message> Messages { get; set; } = new List<Message>();

    public virtual Room Room { get; set; } = null!;

    public virtual StudentProfile Student { get; set; } = null!;
}
