using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Roomify.API.DTOs.Messaging;
using Roomify.API.Helpers;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Controllers;

[ApiController]
[Route("api/landlord/conversations/{conversationId:long}/messages")]
[Authorize(Roles = "LANDLORD")]
public class LandlordMessagesController : ControllerBase
{
    private readonly IMessageService _messageService;

    public LandlordMessagesController(
        IMessageService messageService)
    {
        _messageService = messageService;
    }

    // Lấy messages
    [HttpGet]
    public async Task<ActionResult<List<MessageResponse>>> GetMessages(
        long conversationId)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _messageService.GetLandlordMessagesAsync(
                userId,
                conversationId);

        return Ok(result);
    }

    // Landlord gửi message
    [HttpPost]
    public async Task<ActionResult<MessageResponse>> SendMessage(
        long conversationId,
        [FromBody] SendMessageRequest request)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _messageService.SendLandlordMessageAsync(
                userId,
                conversationId,
                request);

        return StatusCode(201, result);
    }

    // Đánh dấu đã đọc
    [HttpPatch("read")]
    public async Task<IActionResult> MarkAsRead(
        long conversationId)
    {
        var userId = User.GetRequiredUserId();

        var updatedCount =
            await _messageService
                .MarkLandlordConversationReadAsync(
                    userId,
                    conversationId);

        return Ok(new
        {
            updatedCount
        });
    }
}