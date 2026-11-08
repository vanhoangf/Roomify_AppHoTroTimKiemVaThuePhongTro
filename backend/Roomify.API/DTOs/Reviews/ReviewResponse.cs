namespace Roomify.API.DTOs.Reviews;

public class ReviewResponse
{
    public long ReviewId { get; set; }

    public long ContractId { get; set; }

    public long RoomId { get; set; }
    public string RoomTitle { get; set; } = string.Empty;

    public long StudentId { get; set; }
    public string StudentName { get; set; } = string.Empty;

    public byte Rating { get; set; }
    public string? Comment { get; set; }

    public string Status { get; set; } = string.Empty;

    public DateTime CreatedAt { get; set; }
    public DateTime UpdatedAt { get; set; }
}