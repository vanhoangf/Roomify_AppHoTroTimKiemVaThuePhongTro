using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Roomify.API.DTOs.Notifications;
using Roomify.API.Helpers;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Controllers;

[ApiController]
[Route("api/notifications")]
[Authorize]
public class NotificationsController : ControllerBase
{
    private readonly INotificationService _notificationService;

    public NotificationsController(
        INotificationService notificationService)
    {
        _notificationService = notificationService;
    }

    // Danh sách notification
    [HttpGet]
    public async Task<ActionResult<NotificationListResponse>> GetList(
        [FromQuery] bool? isRead = null,
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 20)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _notificationService.GetNotificationsAsync(
                userId,
                isRead,
                page,
                pageSize);

        return Ok(result);
    }

    // Số notification chưa đọc
    [HttpGet("unread-count")]
    public async Task<IActionResult> GetUnreadCount()
    {
        var userId = User.GetRequiredUserId();

        var unreadCount =
            await _notificationService.GetUnreadCountAsync(
                userId);

        return Ok(new
        {
            unreadCount
        });
    }

    // Đánh dấu một notification đã đọc
    [HttpPatch("{notificationId:long}/read")]
    public async Task<ActionResult<NotificationResponse>> MarkAsRead(
        long notificationId)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _notificationService.MarkAsReadAsync(
                userId,
                notificationId);

        return Ok(result);
    }

    // Đánh dấu toàn bộ notification đã đọc
    [HttpPatch("read-all")]
    public async Task<IActionResult> MarkAllAsRead()
    {
        var userId = User.GetRequiredUserId();

        var updatedCount =
            await _notificationService.MarkAllAsReadAsync(
                userId);

        return Ok(new
        {
            updatedCount
        });
    }
}