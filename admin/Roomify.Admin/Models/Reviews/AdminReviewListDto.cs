namespace Roomify.Admin.Models.Reviews;

public class AdminReviewListDto
{
    public List<AdminReviewDto> Items { get; set; } = [];

    public int TotalCount { get; set; }
    public int TotalReviews { get; set; }

    public int VisibleReviews { get; set; }

    public int HiddenReviews { get; set; }

    public double AverageRating { get; set; }


    public int Page { get; set; }

    public int PageSize { get; set; }

    public int TotalPages { get; set; }

    public bool HasNextPage { get; set; }

    public bool HasPreviousPage { get; set; }
}