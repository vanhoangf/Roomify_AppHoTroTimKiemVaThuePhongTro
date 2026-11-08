namespace Roomify.API.DTOs.Admin.Users;

public class AdminUserListResponse
{
    public List<AdminUserResponse> Items { get; set; } = [];

    public int TotalCount { get; set; }

    public int Page { get; set; }

    public int PageSize { get; set; }

    public int TotalPages { get; set; }

    public bool HasNextPage { get; set; }

    public bool HasPreviousPage { get; set; }
}