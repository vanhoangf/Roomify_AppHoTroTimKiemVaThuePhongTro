namespace Roomify.API.DTOs.Admin.Reports;

public class AdminReportResponse
{
    public long ReportId { get; set; }

    public long ReporterUserId { get; set; }

    public string ReporterName { get; set; } = string.Empty;

    public string ReporterEmail { get; set; } = string.Empty;

    public long RoomId { get; set; }

    public string RoomTitle { get; set; } = string.Empty;

    public string Reason { get; set; } = string.Empty;

    public string? Description { get; set; }

    public string Status { get; set; } = string.Empty;

    public string? AdminNote { get; set; }

    public long? ResolvedByUserId { get; set; }

    public string? ResolvedByUserName { get; set; }

    public DateTime? ResolvedAt { get; set; }

    public DateTime CreatedAt { get; set; }
}