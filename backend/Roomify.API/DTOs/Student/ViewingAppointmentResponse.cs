
namespace Roomify.API.DTOs.Student;

public class ViewingAppointmentResponse
{
    public long AppointmentId { get; set; }

    public long RoomId { get; set; }

    public string RoomTitle { get; set; } = string.Empty;

    public string PropertyName { get; set; } = string.Empty;

    public long StudentId { get; set; }

    public string StudentName { get; set; } = string.Empty;

    public string? StudentPhone { get; set; }

    public DateTime ScheduledAt { get; set; }

    public DateTime? ProposedAt { get; set; }

    public string Status { get; set; } = string.Empty;

    public string? Note { get; set; }

    public string? LandlordNote { get; set; }

    public DateTime? RespondedAt { get; set; }

    public DateTime CreatedAt { get; set; }

    public DateTime UpdatedAt { get; set; }
}

public class ViewingAppointmentHistoryResponse
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
