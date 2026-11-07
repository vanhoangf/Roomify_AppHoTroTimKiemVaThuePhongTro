using Roomify.API.DTOs.Notifications;

namespace Roomify.API.Services.Interfaces;

public interface INotificationService
{
    // Danh sách thông báo của user hiện tại
    Task<NotificationListResponse> GetNotificationsAsync(
        long userId,
        bool? isRead,
        int page,
        int pageSize);

    // Tổng số thông báo chưa đọc
    Task<int> GetUnreadCountAsync(long userId);

    // Đánh dấu một notification là đã đọc
    Task<NotificationResponse> MarkAsReadAsync(
        long userId,
        long notificationId);

    // Đánh dấu tất cả notification của user là đã đọc
    Task<int> MarkAllAsReadAsync(long userId);

    // Dùng nội bộ để tạo notification từ các service khác
    Task<NotificationResponse> CreateAsync(
        long userId,
        string? notificationType,
        string title,
        string message,
        string? relatedEntityType = null,
        long? relatedEntityId = null);
}