
namespace Roomify.API.DTOs.Student;

public class CreateViewingAppointmentRequest
{
    public DateTimeOffset ScheduledAt { get; set; }

    public string? Note { get; set; }
}

public class ProposeViewingTimeRequest
{
    public DateTimeOffset ProposedAt { get; set; }

    public string? Note { get; set; }
}

public class ViewingAppointmentNoteRequest
{
    public string? Note { get; set; }
}
