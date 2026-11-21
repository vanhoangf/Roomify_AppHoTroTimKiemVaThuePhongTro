namespace Roomify.Admin.Models.Statistics;

public class AdminStatisticsViewModel
{
    public AdminStatisticsDto? Statistics { get; set; }

    public string? ErrorMessage { get; set; }

    public string Range { get; set; } = "30";

    public DateOnly? CustomFromDate { get; set; }

    public DateOnly? CustomToDate { get; set; }


    public bool HasError =>
        !string.IsNullOrWhiteSpace(
            ErrorMessage);
}