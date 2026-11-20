using Roomify.Admin.Models.Dashboard;

namespace Roomify.Admin.Models.Users;

public class AdminUsersViewModel
{
    public AdminUserListDto? Users { get; set; }

    public AdminDashboardDto? Dashboard { get; set; }

    public string? ErrorMessage { get; set; }

    public string? StatisticsErrorMessage { get; set; }

    public string? Search { get; set; }

    public string? Role { get; set; }

    public string? Status { get; set; }

    public bool? Verified { get; set; }

    public int Page { get; set; } = 1;

    public int PageSize { get; set; } = 8;


    public bool HasError =>
        !string.IsNullOrWhiteSpace(ErrorMessage);


    public bool IsEmpty =>
        Users is not null &&
        Users.Items.Count == 0;


    public double StudentPercentage =>
        Percentage(
            Dashboard?.TotalStudents ?? 0,
            Dashboard?.TotalUsers ?? 0);


    public double LandlordPercentage =>
        Percentage(
            Dashboard?.TotalLandlords ?? 0,
            Dashboard?.TotalUsers ?? 0);


    public double LockedPercentage =>
        Percentage(
            Dashboard?.LockedUsers ?? 0,
            Dashboard?.TotalUsers ?? 0);


    private static double Percentage(
        int value,
        int total)
    {
        if (total <= 0)
        {
            return 0;
        }

        return Math.Round(
            value * 100d / total,
            1);
    }
}