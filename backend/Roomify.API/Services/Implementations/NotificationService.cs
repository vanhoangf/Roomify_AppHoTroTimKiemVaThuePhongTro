using Microsoft.EntityFrameworkCore;
using Roomify.API.Data;
using Roomify.API.DTOs.Notifications;
using Roomify.API.Entities;
using Roomify.API.Exceptions;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Services.Implementations;

public class NotificationService : INotificationService
{
    private readonly RoomifyDbContext _db;

    public NotificationService(RoomifyDbContext db)
    {
        _db = db;
    }

    // ============================================================
    // GET LIST
    // ============================================================

    public async Task<NotificationListResponse> GetNotificationsAsync(
        long userId,
        bool? isRead,
        int page,
        int pageSize)
    {
        ValidatePagination(page, pageSize);

        var query = _db.Notifications
            .AsNoTracking()
            .Where(x => x.UserId == userId);

        if (isRead.HasValue)
        {
            query = query.Where(x => x.IsRead == isRead.Value);
        }

        var totalCount = await query.CountAsync();

        var unreadCount = await _db.Notifications
            .AsNoTracking()
            .CountAsync(x =>
                x.UserId == userId &&
                !x.IsRead);

        var items = await query
            .OrderByDescending(x => x.CreatedAt)
            .ThenByDescending(x => x.NotificationId)
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .Select(x => new NotificationResponse
            {
                NotificationId = x.NotificationId,
                UserId = x.UserId,

                NotificationType = x.NotificationType,
                Title = x.Title,
                Message = x.Message,

                RelatedEntityType = x.RelatedEntityType,
                RelatedEntityId = x.RelatedEntityId,

                IsRead = x.IsRead,
                ReadAt = x.ReadAt,
                CreatedAt = x.CreatedAt
            })
            .ToListAsync();

        var totalPages =
            totalCount == 0
                ? 0
                : (int)Math.Ceiling(
                    totalCount / (double)pageSize);

        return new NotificationListResponse
        {
            Items = items,
            TotalCount = totalCount,
            UnreadCount = unreadCount,

            Page = page,
            PageSize = pageSize,
            TotalPages = totalPages,

            HasNextPage = page < totalPages,
            HasPreviousPage = page > 1
        };
    }

    // ============================================================
    // UNREAD COUNT
    // ============================================================

    public async Task<int> GetUnreadCountAsync(long userId)
    {
        return await _db.Notifications
            .AsNoTracking()
            .CountAsync(x =>
                x.UserId == userId &&
                !x.IsRead);
    }

    // ============================================================
    // MARK ONE AS READ
    // ============================================================

    public async Task<NotificationResponse> MarkAsReadAsync(
        long userId,
        long notificationId)
    {
        var notification = await _db.Notifications
            .FirstOrDefaultAsync(x =>
                x.NotificationId == notificationId &&
                x.UserId == userId);

        if (notification is null)
        {
            throw new NotFoundException(
                "NOTIFICATION_NOT_FOUND",
                "Không tìm thấy thông báo.");
        }

        if (!notification.IsRead)
        {
            notification.IsRead = true;
            notification.ReadAt = DateTime.UtcNow;

            await _db.SaveChangesAsync();
        }

        return MapNotification(notification);
    }

    // ============================================================
    // MARK ALL AS READ
    // ============================================================

    public async Task<int> MarkAllAsReadAsync(long userId)
    {
        var now = DateTime.UtcNow;

        var notifications = await _db.Notifications
            .Where(x =>
                x.UserId == userId &&
                !x.IsRead)
            .ToListAsync();

        if (notifications.Count == 0)
        {
            return 0;
        }

        foreach (var notification in notifications)
        {
            notification.IsRead = true;
            notification.ReadAt = now;
        }

        await _db.SaveChangesAsync();

        return notifications.Count;
    }

    // ============================================================
    // CREATE NOTIFICATION
    // ============================================================

    public async Task<NotificationResponse> CreateAsync(
        long userId,
        string? notificationType,
        string title,
        string message,
        string? relatedEntityType,
        long? relatedEntityId)
    {
        var userExists = await _db.Users
            .AsNoTracking()
            .AnyAsync(x => x.UserId == userId);

        if (!userExists)
        {
            throw new NotFoundException(
                "USER_NOT_FOUND",
                "Không tìm thấy người dùng.");
        }

        if (string.IsNullOrWhiteSpace(title))
        {
            throw new BadRequestException(
                "NOTIFICATION_TITLE_REQUIRED",
                "Tiêu đề thông báo không được để trống.");
        }

        if (string.IsNullOrWhiteSpace(message))
        {
            throw new BadRequestException(
                "NOTIFICATION_MESSAGE_REQUIRED",
                "Nội dung thông báo không được để trống.");
        }

        notificationType =
            NormalizeOptional(notificationType);

        relatedEntityType =
            NormalizeOptional(relatedEntityType);

        title = title.Trim();
        message = message.Trim();

        if (notificationType?.Length > 50)
        {
            throw new BadRequestException(
                "INVALID_NOTIFICATION_TYPE",
                "Loại thông báo không được vượt quá 50 ký tự.");
        }

        if (title.Length > 255)
        {
            throw new BadRequestException(
                "INVALID_NOTIFICATION_TITLE",
                "Tiêu đề thông báo không được vượt quá 255 ký tự.");
        }

        if (message.Length > 1000)
        {
            throw new BadRequestException(
                "INVALID_NOTIFICATION_MESSAGE",
                "Nội dung thông báo không được vượt quá 1000 ký tự.");
        }

        if (relatedEntityType?.Length > 50)
        {
            throw new BadRequestException(
                "INVALID_RELATED_ENTITY_TYPE",
                "Loại đối tượng liên quan không được vượt quá 50 ký tự.");
        }

        var notification = new Notification
        {
            UserId = userId,

            NotificationType = notificationType,
            Title = title,
            Message = message,

            RelatedEntityType = relatedEntityType,
            RelatedEntityId = relatedEntityId,

            IsRead = false,
            ReadAt = null,
            CreatedAt = DateTime.UtcNow
        };

        _db.Notifications.Add(notification);

        await _db.SaveChangesAsync();

        return MapNotification(notification);
    }

    // ============================================================
    // PRIVATE HELPERS
    // ============================================================

    private static NotificationResponse MapNotification(
        Notification notification)
    {
        return new NotificationResponse
        {
            NotificationId = notification.NotificationId,
            UserId = notification.UserId,

            NotificationType = notification.NotificationType,
            Title = notification.Title,
            Message = notification.Message,

            RelatedEntityType = notification.RelatedEntityType,
            RelatedEntityId = notification.RelatedEntityId,

            IsRead = notification.IsRead,
            ReadAt = notification.ReadAt,
            CreatedAt = notification.CreatedAt
        };
    }

    private static string? NormalizeOptional(string? value)
    {
        if (string.IsNullOrWhiteSpace(value))
        {
            return null;
        }

        return value.Trim();
    }

    private static void ValidatePagination(
        int page,
        int pageSize)
    {
        if (page < 1 || page > 100000)
        {
            throw new BadRequestException(
                "INVALID_PAGE",
                "Page phải nằm trong khoảng từ 1 đến 100000.");
        }

        if (pageSize < 1 || pageSize > 50)
        {
            throw new BadRequestException(
                "INVALID_PAGE_SIZE",
                "PageSize phải nằm trong khoảng từ 1 đến 50.");
        }
    }
}