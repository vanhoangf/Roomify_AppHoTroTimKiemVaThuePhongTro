namespace Roomify.API.Entities;

public class ViewingAppointmentHistory
{
    public long AppointmentHistoryId { get; set; }

    public long AppointmentId { get; set; }

    public long ActorUserId { get; set; }

    public string Action { get; set; } = string.Empty;

    public string? OldStatus { get; set; }

    public string NewStatus { get; set; } = string.Empty;

    public DateTime? OldScheduledAt { get; set; }

    public DateTime? NewScheduledAt { get; set; }

    public string? Note { get; set; }

    public DateTime CreatedAt { get; set; }
}