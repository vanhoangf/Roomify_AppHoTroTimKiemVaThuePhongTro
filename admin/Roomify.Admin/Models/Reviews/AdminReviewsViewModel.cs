namespace Roomify.Admin.Models.Reviews;

public class AdminReviewsViewModel
{
    public AdminReviewListDto? Reviews { get; set; }

    public string? ErrorMessage { get; set; }

    public string? Search { get; set; }

    public string? Status { get; set; }

    public int? Rating { get; set; }

    public int Page { get; set; } = 1;

    public int PageSize { get; set; } = 10;


    public bool HasError =>
        !string.IsNullOrWhiteSpace(ErrorMessage);


    public bool IsEmpty =>
        Reviews is not null &&
        Reviews.Items.Count == 0;
}