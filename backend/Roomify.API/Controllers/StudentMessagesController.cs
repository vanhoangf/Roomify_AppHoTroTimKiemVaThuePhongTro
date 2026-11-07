using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Roomify.API.DTOs.Messaging;
using Roomify.API.Helpers;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Controllers;

[ApiController]
[Route("api/student/conversations/{conversationId:long}/messages")]
[Authorize(Roles = "STUDENT")]
public class StudentMessagesController : ControllerBase
{
    private readonly IMessageService _messageService;

    public StudentMessagesController(
        IMessageService messageService)
    {
        _messageService = messageService;
    }

    // Lấy toàn bộ message trong conversation
    [HttpGet]
    public async Task<ActionResult<List<MessageResponse>>> GetMessages(
        long conversationId)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _messageService.GetStudentMessagesAsync(
                userId,
                conversationId);

        return Ok(result);
    }

    // Student gửi message
    [HttpPost]
    public async Task<ActionResult<MessageResponse>> SendMessage(
        long conversationId,
        [FromBody] SendMessageRequest request)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _messageService.SendStudentMessageAsync(
                userId,
                conversationId,
                request);

        return StatusCode(201, result);
    }

    // Đánh dấu message phía đối phương đã đọc
    [HttpPatch("read")]
    public async Task<IActionResult> MarkAsRead(
        long conversationId)
    {
        var userId = User.GetRequiredUserId();

        var updatedCount =
            await _messageService
                .MarkStudentConversationReadAsync(
                    userId,
                    conversationId);

        return Ok(new
        {
            updatedCount
        });
    }
}