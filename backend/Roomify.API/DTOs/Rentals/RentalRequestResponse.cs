namespace Roomify.API.DTOs.Rentals;

public class RentalRequestResponse
{
    public long RentalRequestId { get; set; }

    public long RoomId { get; set; }

    public string RoomTitle { get; set; } = string.Empty;

    public long PropertyId { get; set; }

    public string PropertyName { get; set; } = string.Empty;

    public long StudentId { get; set; }

    public string StudentName { get; set; } = string.Empty;

    public string? StudentPhone { get; set; }

    public DateOnly RequestedMoveInDate { get; set; }

    public int? DurationMonths { get; set; }

    public string? Note { get; set; }

    public string Status { get; set; } = string.Empty;

    public string? LandlordNote { get; set; }

    public DateTime? RespondedAt { get; set; }

    public DateTime CreatedAt { get; set; }

    public DateTime UpdatedAt { get; set; }
}