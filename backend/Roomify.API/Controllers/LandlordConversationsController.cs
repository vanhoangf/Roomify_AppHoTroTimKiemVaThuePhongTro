using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Roomify.API.DTOs.Messaging;
using Roomify.API.Helpers;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Controllers;

[ApiController]
[Route("api/landlord/conversations")]
[Authorize(Roles = "LANDLORD")]
public class LandlordConversationsController : ControllerBase
{
    private readonly IConversationService _conversationService;

    public LandlordConversationsController(
        IConversationService conversationService)
    {
        _conversationService = conversationService;
    }

    // Danh sách conversation của landlord
    [HttpGet]
    public async Task<ActionResult<ConversationListResponse>> GetList(
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 20)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _conversationService
                .GetLandlordConversationsAsync(
                    userId,
                    page,
                    pageSize);

        return Ok(result);
    }

    // Chi tiết conversation thuộc landlord
    [HttpGet("{conversationId:long}")]
    public async Task<ActionResult<ConversationResponse>> GetById(
        long conversationId)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _conversationService
                .GetLandlordConversationByIdAsync(
                    userId,
                    conversationId);

        return Ok(result);
    }
}