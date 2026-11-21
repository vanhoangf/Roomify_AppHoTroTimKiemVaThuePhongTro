using Roomify.Admin.Models.Dashboard;

namespace Roomify.Admin.Models.Reports;

public class AdminReportsViewModel
{
    public AdminReportListDto? Reports { get; set; }

    public AdminDashboardDto? Dashboard { get; set; }

    public string? ErrorMessage { get; set; }

    public string? StatisticsErrorMessage { get; set; }

    public string? Search { get; set; }

    public string? Status { get; set; }

    public int Page { get; set; } = 1;

    public int PageSize { get; set; } = 10;


    public bool HasError =>
        !string.IsNullOrWhiteSpace(ErrorMessage);


    public bool IsEmpty =>
        Reports is not null &&
        Reports.Items.Count == 0;


    public int TotalReports =>
        (Dashboard?.OpenReports ?? 0) +
        (Dashboard?.ResolvedReports ?? 0) +
        (Dashboard?.RejectedReports ?? 0);
}