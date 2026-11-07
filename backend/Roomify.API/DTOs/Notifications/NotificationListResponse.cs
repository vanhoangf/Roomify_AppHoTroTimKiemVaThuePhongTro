namespace Roomify.API.DTOs.Notifications;

public class NotificationListResponse
{
    public List<NotificationResponse> Items { get; set; } = new();

    public int TotalCount { get; set; }

    public int UnreadCount { get; set; }

    public int Page { get; set; }

    public int PageSize { get; set; }

    public int TotalPages { get; set; }

    public bool HasNextPage { get; set; }

    public bool HasPreviousPage { get; set; }
}