using System.ComponentModel.DataAnnotations;

namespace Roomify.API.DTOs.Reports;

public class CreateReportRequest
{
    [Required]
    [MaxLength(255)]
    public string Reason { get; set; } = string.Empty;

    [MaxLength(2000)]
    public string? Description { get; set; }
}