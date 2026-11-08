using System.ComponentModel.DataAnnotations;

namespace Roomify.API.DTOs.Reviews;

public class CreateReviewRequest
{
    [Range(1, 5)]
    public byte Rating { get; set; }

    [MaxLength(2000)]
    public string? Comment { get; set; }
}