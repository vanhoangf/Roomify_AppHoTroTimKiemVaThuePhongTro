using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Roomify.API.DTOs.Messaging;
using Roomify.API.Helpers;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Controllers;

[ApiController]
[Route("api/student")]
[Authorize(Roles = "STUDENT")]
public class StudentConversationsController : ControllerBase
{
    private readonly IConversationService _conversationService;

    public StudentConversationsController(
        IConversationService conversationService)
    {
        _conversationService = conversationService;
    }

    // Tạo hoặc lấy conversation giữa student và room
    [HttpPost("rooms/{roomId:long}/conversation")]
    public async Task<ActionResult<ConversationResponse>> GetOrCreate(
        long roomId)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _conversationService
                .GetOrCreateStudentConversationAsync(
                    userId,
                    roomId);

        return Ok(result);
    }

    // Danh sách conversation của student
    [HttpGet("conversations")]
    public async Task<ActionResult<ConversationListResponse>> GetList(
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 20)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _conversationService
                .GetStudentConversationsAsync(
                    userId,
                    page,
                    pageSize);

        return Ok(result);
    }

    // Chi tiết conversation
    [HttpGet("conversations/{conversationId:long}")]
    public async Task<ActionResult<ConversationResponse>> GetById(
        long conversationId)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _conversationService
                .GetStudentConversationByIdAsync(
                    userId,
                    conversationId);

        return Ok(result);
    }
}