using Microsoft.EntityFrameworkCore;
using Roomify.API.Data;
using Roomify.API.DTOs.Messaging;
using Roomify.API.Entities;
using Roomify.API.Exceptions;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Services.Implementations;

public class MessageService : IMessageService
{
    private readonly RoomifyDbContext _db;
    private readonly INotificationService _notificationService;

    public MessageService(
        RoomifyDbContext db,
        INotificationService notificationService)
    {
        _db = db;
        _notificationService = notificationService;
    }

    // =========================================================
    // STUDENT
    // =========================================================

    public async Task<List<MessageResponse>> GetStudentMessagesAsync(
        long userId,
        long conversationId)
    {
        await EnsureStudentConversationAccessAsync(
            userId,
            conversationId);

        return await GetMessagesAsync(
            conversationId,
            userId);
    }

    public async Task<MessageResponse> SendStudentMessageAsync(
        long userId,
        long conversationId,
        SendMessageRequest request)
    {
        var conversation =
            await GetStudentConversationForUpdateAsync(
                userId,
                conversationId);

        return await SendMessageAsync(
            userId,
            conversation,
            request);
    }

    public async Task<int> MarkStudentConversationReadAsync(
        long userId,
        long conversationId)
    {
        await EnsureStudentConversationAccessAsync(
            userId,
            conversationId);

        return await MarkConversationReadAsync(
            userId,
            conversationId);
    }


    // =========================================================
    // LANDLORD
    // =========================================================

    public async Task<List<MessageResponse>> GetLandlordMessagesAsync(
        long userId,
        long conversationId)
    {
        await EnsureLandlordConversationAccessAsync(
            userId,
            conversationId);

        return await GetMessagesAsync(
            conversationId,
            userId);
    }

    public async Task<MessageResponse> SendLandlordMessageAsync(
        long userId,
        long conversationId,
        SendMessageRequest request)
    {
        var conversation =
            await GetLandlordConversationForUpdateAsync(
                userId,
                conversationId);

        return await SendMessageAsync(
            userId,
            conversation,
            request);
    }

    public async Task<int> MarkLandlordConversationReadAsync(
        long userId,
        long conversationId)
    {
        await EnsureLandlordConversationAccessAsync(
            userId,
            conversationId);

        return await MarkConversationReadAsync(
            userId,
            conversationId);
    }


    // =========================================================
    // GET MESSAGES
    // =========================================================

    private async Task<List<MessageResponse>> GetMessagesAsync(
        long conversationId,
        long currentUserId)
    {
        return await _db.Messages
            .AsNoTracking()
            .Where(x =>
                x.ConversationId == conversationId)
            .OrderBy(x => x.SentAt)
            .ThenBy(x => x.MessageId)
            .Select(x => new MessageResponse
            {
                MessageId = x.MessageId,
                ConversationId = x.ConversationId,

                SenderUserId = x.SenderUserId,
                SenderName = x.SenderUser.FullName,
                SenderRole = x.SenderUser.Role.ToString(),

                Content = x.Content,
                ImageUrl = x.ImageUrl,
                MessageType = x.MessageType,

                IsMine = x.SenderUserId == currentUserId,

                IsRead = x.IsRead,
                ReadAt = x.ReadAt,
                SentAt = x.SentAt
            })
            .ToListAsync();
    }


    // =========================================================
    // SEND MESSAGE
    // =========================================================

    private async Task<MessageResponse> SendMessageAsync(
    long userId,
    Conversation conversation,
    SendMessageRequest request)
    {
        var messageType = NormalizeMessageType(
            request.MessageType);

        string? content = null;
        string? imageUrl = null;

        // -----------------------------------------------------
        // TEXT
        // -----------------------------------------------------

        if (messageType == "TEXT")
        {
            if (string.IsNullOrWhiteSpace(request.Content))
            {
                throw new BadRequestException(
                    "MESSAGE_CONTENT_REQUIRED",
                    "Nội dung tin nhắn không được để trống.");
            }

            content = request.Content.Trim();

            if (content.Length > 2000)
            {
                throw new BadRequestException(
                    "MESSAGE_CONTENT_TOO_LONG",
                    "Nội dung tin nhắn không được vượt quá 2000 ký tự.");
            }
        }

        // -----------------------------------------------------
        // IMAGE
        // -----------------------------------------------------

        if (messageType == "IMAGE")
        {
            if (string.IsNullOrWhiteSpace(request.ImageUrl))
            {
                throw new BadRequestException(
                    "MESSAGE_IMAGE_REQUIRED",
                    "Vui lòng cung cấp đường dẫn hình ảnh.");
            }

            imageUrl = request.ImageUrl.Trim();

            if (imageUrl.Length > 1000)
            {
                throw new BadRequestException(
                    "MESSAGE_IMAGE_URL_TOO_LONG",
                    "Đường dẫn hình ảnh không được vượt quá 1000 ký tự.");
            }
        }

        var now = DateTime.UtcNow;

        // Xác định người nhận notification
        var recipientUserId = await GetRecipientUserIdAsync(
            conversation.ConversationId,
            userId);

        await using var transaction =
            await _db.Database.BeginTransactionAsync();

        try
        {
            var message = new Message
            {
                ConversationId = conversation.ConversationId,

                SenderUserId = userId,

                Content = content,
                ImageUrl = imageUrl,

                MessageType = messageType,

                IsRead = false,
                ReadAt = null,

                SentAt = now
            };

            _db.Messages.Add(message);

            // Cập nhật thời gian tin nhắn cuối cùng
            conversation.LastMessageAt = now;

            await _db.SaveChangesAsync();

            // Tạo notification cho người còn lại trong conversation
            await _notificationService.CreateAsync(
                recipientUserId,
                "MESSAGE",
                "Tin nhắn mới",
                messageType == "IMAGE"
                    ? "Bạn vừa nhận được một hình ảnh mới."
                    : "Bạn vừa nhận được một tin nhắn mới.",
                "CONVERSATION",
                conversation.ConversationId);

            await transaction.CommitAsync();

            return await GetMessageByIdAsync(
                message.MessageId,
                userId);
        }
        catch
        {
            await transaction.RollbackAsync();
            throw;
        }
    }


    // =========================================================
    // MARK READ
    // =========================================================

    private async Task<int> MarkConversationReadAsync(
        long currentUserId,
        long conversationId)
    {
        var unreadMessages = await _db.Messages
            .Where(x =>
                x.ConversationId == conversationId &&
                x.SenderUserId != currentUserId &&
                !x.IsRead)
            .ToListAsync();

        if (unreadMessages.Count == 0)
        {
            return 0;
        }

        var now = DateTime.UtcNow;

        foreach (var message in unreadMessages)
        {
            message.IsRead = true;
            message.ReadAt = now;
        }

        await _db.SaveChangesAsync();

        return unreadMessages.Count;
    }


    // =========================================================
    // STUDENT OWNERSHIP
    // =========================================================

    private async Task EnsureStudentConversationAccessAsync(
        long userId,
        long conversationId)
    {
        var exists = await _db.Conversations
            .AsNoTracking()
            .AnyAsync(x =>
                x.ConversationId == conversationId &&
                x.Student.UserId == userId);

        if (!exists)
        {
            throw new NotFoundException(
                "CONVERSATION_NOT_FOUND",
                "Không tìm thấy cuộc trò chuyện.");
        }
    }

    private async Task<Conversation>
        GetStudentConversationForUpdateAsync(
            long userId,
            long conversationId)
    {
        var conversation = await _db.Conversations
            .FirstOrDefaultAsync(x =>
                x.ConversationId == conversationId &&
                x.Student.UserId == userId);

        if (conversation is null)
        {
            throw new NotFoundException(
                "CONVERSATION_NOT_FOUND",
                "Không tìm thấy cuộc trò chuyện.");
        }

        return conversation;
    }


    // =========================================================
    // LANDLORD OWNERSHIP
    // =========================================================

    private async Task EnsureLandlordConversationAccessAsync(
        long userId,
        long conversationId)
    {
        var exists = await _db.Conversations
            .AsNoTracking()
            .AnyAsync(x =>
                x.ConversationId == conversationId &&
                x.Room.Property.Landlord.UserId == userId);

        if (!exists)
        {
            throw new NotFoundException(
                "CONVERSATION_NOT_FOUND",
                "Không tìm thấy cuộc trò chuyện.");
        }
    }

    private async Task<Conversation>
        GetLandlordConversationForUpdateAsync(
            long userId,
            long conversationId)
    {
        var conversation = await _db.Conversations
            .FirstOrDefaultAsync(x =>
                x.ConversationId == conversationId &&
                x.Room.Property.Landlord.UserId == userId);

        if (conversation is null)
        {
            throw new NotFoundException(
                "CONVERSATION_NOT_FOUND",
                "Không tìm thấy cuộc trò chuyện.");
        }

        return conversation;
    }


    // =========================================================
    // GET SINGLE MESSAGE
    // =========================================================

    private async Task<MessageResponse> GetMessageByIdAsync(
        long messageId,
        long currentUserId)
    {
        var response = await _db.Messages
            .AsNoTracking()
            .Where(x =>
                x.MessageId == messageId)
            .Select(x => new MessageResponse
            {
                MessageId = x.MessageId,
                ConversationId = x.ConversationId,

                SenderUserId = x.SenderUserId,
                SenderName = x.SenderUser.FullName,
                SenderRole = x.SenderUser.Role.ToString(),

                Content = x.Content,
                ImageUrl = x.ImageUrl,
                MessageType = x.MessageType,

                IsMine = x.SenderUserId == currentUserId,

                IsRead = x.IsRead,
                ReadAt = x.ReadAt,
                SentAt = x.SentAt
            })
            .FirstOrDefaultAsync();

        if (response is null)
        {
            throw new NotFoundException(
                "MESSAGE_NOT_FOUND",
                "Không tìm thấy tin nhắn.");
        }

        return response;
    }

    // =========================================================
    // RECIPIENT
    // =========================================================

    private async Task<long> GetRecipientUserIdAsync(
        long conversationId,
        long senderUserId)
    {
        var recipientUserId = await _db.Conversations
            .AsNoTracking()
            .Where(x => x.ConversationId == conversationId)
            .Select(x =>
                x.Student.UserId == senderUserId
                    ? x.Room.Property.Landlord.UserId
                    : x.Student.UserId)
            .FirstOrDefaultAsync();

        if (recipientUserId <= 0)
        {
            throw new NotFoundException(
                "CONVERSATION_RECIPIENT_NOT_FOUND",
                "Không tìm thấy người nhận tin nhắn.");
        }

        return recipientUserId;
    }


    // =========================================================
    // VALIDATION
    // =========================================================

    private static string NormalizeMessageType(
        string? messageType)
    {
        var normalized =
            string.IsNullOrWhiteSpace(messageType)
                ? "TEXT"
                : messageType.Trim().ToUpperInvariant();

        if (normalized != "TEXT" &&
            normalized != "IMAGE")
        {
            throw new BadRequestException(
                "INVALID_MESSAGE_TYPE",
                "Loại tin nhắn chỉ có thể là TEXT hoặc IMAGE.");
        }

        return normalized;
    }
}