using System.ComponentModel.DataAnnotations;

namespace Roomify.API.DTOs.Admin.Reports;

public class AdminReportDecisionRequest
{
    [Required]
    [MaxLength(2000)]
    public string AdminNote { get; set; } = string.Empty;
}