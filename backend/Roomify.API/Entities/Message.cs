using System;
using System.Collections.Generic;

namespace Roomify.API.Entities;

public partial class Message
{
    public long MessageId { get; set; }

    public long ConversationId { get; set; }

    public long SenderUserId { get; set; }

    public string? Content { get; set; }

    public string? ImageUrl { get; set; }

    public string MessageType { get; set; } = null!;

    public bool IsRead { get; set; }

    public DateTime? ReadAt { get; set; }

    public DateTime SentAt { get; set; }

    public virtual Conversation Conversation { get; set; } = null!;

    public virtual User SenderUser { get; set; } = null!;
}
