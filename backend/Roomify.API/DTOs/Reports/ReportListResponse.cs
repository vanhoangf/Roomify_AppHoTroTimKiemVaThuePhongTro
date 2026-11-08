namespace Roomify.API.DTOs.Reports;

public class ReportListResponse
{
    public List<ReportResponse> Items { get; set; } = [];

    public int TotalCount { get; set; }

    public int Page { get; set; }
    public int PageSize { get; set; }
    public int TotalPages { get; set; }

    public bool HasNextPage { get; set; }
    public bool HasPreviousPage { get; set; }
}