namespace Roomify.Admin.Models.Reviews;

public class AdminReviewDetailViewModel
{
    public AdminReviewDto? Review { get; set; }

    public string? ErrorMessage { get; set; }

    public string? ReturnUrl { get; set; }


    public bool HasError =>
        !string.IsNullOrWhiteSpace(
            ErrorMessage);


    public bool IsVisible =>
        string.Equals(
            Review?.Status,
            "VISIBLE",
            StringComparison.OrdinalIgnoreCase);


    public bool IsHidden =>
        string.Equals(
            Review?.Status,
            "HIDDEN",
            StringComparison.OrdinalIgnoreCase);
}