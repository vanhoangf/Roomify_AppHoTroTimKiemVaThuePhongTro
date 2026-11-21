namespace Roomify.API.DTOs.Admin.Reviews;

public class AdminReviewListResponse
{
    public List<AdminReviewResponse> Items { get; set; }
        = [];


    public int TotalCount { get; set; }


    // ============================================================
    // GLOBAL STATISTICS
    // ============================================================

    public int TotalReviews { get; set; }

    public int VisibleReviews { get; set; }

    public int HiddenReviews { get; set; }

    public double AverageRating { get; set; }


    // ============================================================
    // PAGINATION
    // ============================================================

    public int Page { get; set; }

    public int PageSize { get; set; }

    public int TotalPages { get; set; }

    public bool HasNextPage { get; set; }

    public bool HasPreviousPage { get; set; }
}