namespace Roomify.API.DTOs.Messaging;

public class MessageResponse
{
    public long MessageId { get; set; }

    public long ConversationId { get; set; }

    public long SenderUserId { get; set; }

    public string SenderName { get; set; } = null!;

    public string SenderRole { get; set; } = null!;

    public string? Content { get; set; }

    public string? ImageUrl { get; set; }

    public string MessageType { get; set; } = null!;

    public bool IsMine { get; set; }

    public bool IsRead { get; set; }

    public DateTime? ReadAt { get; set; }

    public DateTime SentAt { get; set; }
}