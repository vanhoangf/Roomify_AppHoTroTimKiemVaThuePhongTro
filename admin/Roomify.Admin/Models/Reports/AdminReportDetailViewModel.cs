namespace Roomify.Admin.Models.Reports;

public class AdminReportDetailViewModel
{
    public AdminReportDto? Report { get; set; }

    public string? ErrorMessage { get; set; }

    public string? ReturnUrl { get; set; }


    public bool HasError =>
        !string.IsNullOrWhiteSpace(ErrorMessage);


    public bool IsOpen =>
        string.Equals(
            Report?.Status,
            "OPEN",
            StringComparison.OrdinalIgnoreCase);


    public bool IsResolved =>
        string.Equals(
            Report?.Status,
            "RESOLVED",
            StringComparison.OrdinalIgnoreCase);


    public bool IsRejected =>
        string.Equals(
            Report?.Status,
            "REJECTED",
            StringComparison.OrdinalIgnoreCase);
}