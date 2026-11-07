namespace Roomify.API.DTOs.Messaging;

public class ConversationResponse
{
    public long ConversationId { get; set; }

    public long RoomId { get; set; }
    public string RoomTitle { get; set; } = null!;

    public long StudentId { get; set; }
    public long StudentUserId { get; set; }
    public string StudentName { get; set; } = null!;

    public long LandlordId { get; set; }
    public long LandlordUserId { get; set; }
    public string LandlordName { get; set; } = null!;

    public string? LastMessage { get; set; }
    public string? LastMessageType { get; set; }

    public DateTime? LastMessageAt { get; set; }

    public int UnreadCount { get; set; }

    public DateTime CreatedAt { get; set; }
}