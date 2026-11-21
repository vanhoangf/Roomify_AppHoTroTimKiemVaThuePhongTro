namespace Roomify.API.DTOs.Admin.Reviews;

public class AdminReviewResponse
{
    public long ReviewId { get; set; }

    public long ContractId { get; set; }

    public string ContractCode { get; set; }
        = string.Empty;


    public long RoomId { get; set; }

    public string RoomTitle { get; set; }
        = string.Empty;


    public long PropertyId { get; set; }

    public string PropertyName { get; set; }
        = string.Empty;


    public long StudentId { get; set; }

    public long StudentUserId { get; set; }

    public string StudentName { get; set; }
        = string.Empty;

    public string StudentEmail { get; set; }
        = string.Empty;


    public byte Rating { get; set; }

    public string? Comment { get; set; }

    public string Status { get; set; }
        = string.Empty;


    public long? ModeratedByUserId { get; set; }

    public string? ModeratedByUserName { get; set; }

    public DateTime? ModeratedAt { get; set; }


    public DateTime CreatedAt { get; set; }

    public DateTime UpdatedAt { get; set; }
}