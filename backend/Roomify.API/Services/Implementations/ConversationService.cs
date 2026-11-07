using Microsoft.EntityFrameworkCore;
using Roomify.API.Data;
using Roomify.API.DTOs.Messaging;
using Roomify.API.Entities;
using Roomify.API.Exceptions;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Services.Implementations;

public class ConversationService : IConversationService
{
    private readonly RoomifyDbContext _db;

    public ConversationService(RoomifyDbContext db)
    {
        _db = db;
    }

    // =========================================================
    // STUDENT
    // =========================================================

    public async Task<ConversationResponse> GetOrCreateStudentConversationAsync(
    long userId,
    long roomId)
    {
        var student = await _db.StudentProfiles
            .AsNoTracking()
            .FirstOrDefaultAsync(x => x.UserId == userId)
            ?? throw new NotFoundException(
                "STUDENT_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ sinh viên.");

        var room = await _db.Rooms
            .AsNoTracking()
            .FirstOrDefaultAsync(x =>
                x.RoomId == roomId &&
                x.ListingStatus == "PUBLISHED");

        if (room is null)
        {
            throw new NotFoundException(
                "ROOM_NOT_FOUND",
                "Không tìm thấy phòng đang công khai.");
        }

        var existingConversation = await BaseConversationQuery()
            .FirstOrDefaultAsync(x =>
                x.RoomId == roomId &&
                x.StudentId == student.StudentId);

        if (existingConversation is not null)
        {
            return BuildStudentProjection(
                existingConversation,
                userId);
        }

        var now = DateTime.UtcNow;

        var conversation = new Conversation
        {
            RoomId = roomId,
            StudentId = student.StudentId,
            LastMessageAt = null,
            CreatedAt = now
        };

        _db.Conversations.Add(conversation);

        try
        {
            await _db.SaveChangesAsync();
        }
        catch (DbUpdateException)
        {
            _db.Entry(conversation).State =
                EntityState.Detached;

            var duplicatedConversation =
                await BaseConversationQuery()
                    .FirstOrDefaultAsync(x =>
                        x.RoomId == roomId &&
                        x.StudentId == student.StudentId);

            if (duplicatedConversation is not null)
            {
                return BuildStudentProjection(
                    duplicatedConversation,
                    userId);
            }

            throw;
        }

        return await GetStudentConversationByIdAsync(
            userId,
            conversation.ConversationId);
    }

    public async Task<ConversationListResponse> GetStudentConversationsAsync(
    long userId,
    int page,
    int pageSize)
    {
        ValidatePagination(page, pageSize);

        var student = await _db.StudentProfiles
            .AsNoTracking()
            .FirstOrDefaultAsync(x => x.UserId == userId)
            ?? throw new NotFoundException(
                "STUDENT_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ sinh viên.");

        var query = BaseConversationQuery()
            .Where(x => x.StudentId == student.StudentId);

        var totalCount = await query.CountAsync();

        var entities = await query
            .OrderByDescending(x =>
                x.LastMessageAt ?? x.CreatedAt)
            .ThenByDescending(x => x.ConversationId)
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .ToListAsync();

        var items = entities
            .Select(x =>
                BuildStudentProjection(x, userId))
            .ToList();

        return BuildListResponse(
            items,
            totalCount,
            page,
            pageSize);
    }

    public async Task<ConversationResponse> GetStudentConversationByIdAsync(
    long userId,
    long conversationId)
    {
        var conversation = await BaseConversationQuery()
            .FirstOrDefaultAsync(x =>
                x.ConversationId == conversationId &&
                x.Student.UserId == userId);

        if (conversation is null)
        {
            throw new NotFoundException(
                "CONVERSATION_NOT_FOUND",
                "Không tìm thấy cuộc trò chuyện.");
        }

        return BuildStudentProjection(
            conversation,
            userId);
    }


    // =========================================================
    // LANDLORD
    // =========================================================

    public async Task<ConversationListResponse> GetLandlordConversationsAsync(
    long userId,
    int page,
    int pageSize)
    {
        ValidatePagination(page, pageSize);

        var landlord = await _db.LandlordProfiles
            .AsNoTracking()
            .FirstOrDefaultAsync(x => x.UserId == userId)
            ?? throw new NotFoundException(
                "LANDLORD_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ chủ trọ.");

        var query = BaseConversationQuery()
            .Where(x =>
                x.Room.Property.LandlordId ==
                landlord.LandlordId);

        var totalCount = await query.CountAsync();

        var entities = await query
            .OrderByDescending(x =>
                x.LastMessageAt ?? x.CreatedAt)
            .ThenByDescending(x => x.ConversationId)
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .ToListAsync();

        var items = entities
            .Select(x =>
                BuildLandlordProjection(x, userId))
            .ToList();

        return BuildListResponse(
            items,
            totalCount,
            page,
            pageSize);
    }

    public async Task<ConversationResponse> GetLandlordConversationByIdAsync(
        long userId,
        long conversationId)
    {
        var conversation = await BaseConversationQuery()
            .FirstOrDefaultAsync(x =>
                x.ConversationId == conversationId &&
                x.Room.Property.Landlord.UserId == userId);

        if (conversation is null)
        {
            throw new NotFoundException(
                "CONVERSATION_NOT_FOUND",
                "Không tìm thấy cuộc trò chuyện.");
        }

        return BuildLandlordProjection(
            conversation,
            userId);
    }


    // =========================================================
    // PROJECTION
    // =========================================================

    private static ConversationResponse BuildStudentProjection(
        Conversation x,
        long currentUserId)
    {
        var lastMessage = x.Messages
            .OrderByDescending(m => m.SentAt)
            .ThenByDescending(m => m.MessageId)
            .FirstOrDefault();

        return new ConversationResponse
        {
            ConversationId = x.ConversationId,

            RoomId = x.RoomId,
            RoomTitle = x.Room.Title,

            StudentId = x.StudentId,
            StudentUserId = x.Student.UserId,
            StudentName = x.Student.User.FullName,

            LandlordId = x.Room.Property.LandlordId,
            LandlordUserId = x.Room.Property.Landlord.UserId,
            LandlordName = x.Room.Property.Landlord.User.FullName,

            LastMessage = lastMessage == null
                ? null
                : lastMessage.MessageType == "IMAGE"
                    ? "[Hình ảnh]"
                    : lastMessage.Content,

            LastMessageType = lastMessage == null
                ? null
                : lastMessage.MessageType,

            LastMessageAt = x.LastMessageAt,

            UnreadCount = x.Messages.Count(m =>
                m.SenderUserId != currentUserId &&
                !m.IsRead),

            CreatedAt = x.CreatedAt
        };
    }

    private static ConversationResponse BuildLandlordProjection(
        Conversation x,
        long currentUserId)
    {
        var lastMessage = x.Messages
            .OrderByDescending(m => m.SentAt)
            .ThenByDescending(m => m.MessageId)
            .FirstOrDefault();

        return new ConversationResponse
        {
            ConversationId = x.ConversationId,

            RoomId = x.RoomId,
            RoomTitle = x.Room.Title,

            StudentId = x.StudentId,
            StudentUserId = x.Student.UserId,
            StudentName = x.Student.User.FullName,

            LandlordId = x.Room.Property.LandlordId,
            LandlordUserId = x.Room.Property.Landlord.UserId,
            LandlordName = x.Room.Property.Landlord.User.FullName,

            LastMessage = lastMessage == null
                ? null
                : lastMessage.MessageType == "IMAGE"
                    ? "[Hình ảnh]"
                    : lastMessage.Content,

            LastMessageType = lastMessage == null
                ? null
                : lastMessage.MessageType,

            LastMessageAt = x.LastMessageAt,

            UnreadCount = x.Messages.Count(m =>
                m.SenderUserId != currentUserId &&
                !m.IsRead),

            CreatedAt = x.CreatedAt
        };
    }


    // =========================================================
    // HELPERS
    // =========================================================

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

    private static ConversationListResponse BuildListResponse(
        List<ConversationResponse> items,
        int totalCount,
        int page,
        int pageSize)
    {
        var totalPages = totalCount == 0
            ? 0
            : (int)Math.Ceiling(totalCount / (double)pageSize);

        return new ConversationListResponse
        {
            Items = items,
            TotalCount = totalCount,
            Page = page,
            PageSize = pageSize,
            TotalPages = totalPages,
            HasNextPage = page < totalPages,
            HasPreviousPage = page > 1
        };
    }

    private IQueryable<Conversation> BaseConversationQuery()
    {
        return _db.Conversations
            .AsNoTracking()

            .Include(x => x.Room)
                .ThenInclude(x => x.Property)
                    .ThenInclude(x => x.Landlord)
                        .ThenInclude(x => x.User)

            .Include(x => x.Student)
                .ThenInclude(x => x.User)

            .Include(x => x.Messages);
    }
}