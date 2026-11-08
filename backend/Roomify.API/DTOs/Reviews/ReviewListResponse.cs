namespace Roomify.API.DTOs.Reviews;

public class ReviewListResponse
{
    public List<ReviewResponse> Items { get; set; } = [];

    public int TotalCount { get; set; }

    public double AverageRating { get; set; }

    public int Page { get; set; }
    public int PageSize { get; set; }
    public int TotalPages { get; set; }

    public bool HasNextPage { get; set; }
    public bool HasPreviousPage { get; set; }
}