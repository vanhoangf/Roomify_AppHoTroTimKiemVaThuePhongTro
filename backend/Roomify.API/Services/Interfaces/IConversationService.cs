using Roomify.API.DTOs.Messaging;

namespace Roomify.API.Services.Interfaces;

public interface IConversationService
{
    // STUDENT
    // Lấy conversation hiện có hoặc tạo mới giữa Student và Room
    Task<ConversationResponse> GetOrCreateStudentConversationAsync(
        long userId,
        long roomId);

    // Danh sách conversation của Student
    Task<ConversationListResponse> GetStudentConversationsAsync(
        long userId,
        int page,
        int pageSize);

    // Student xem chi tiết conversation của chính mình
    Task<ConversationResponse> GetStudentConversationByIdAsync(
        long userId,
        long conversationId);
    // LANDLORD
    // Danh sách conversation thuộc các phòng của Landlord
    Task<ConversationListResponse> GetLandlordConversationsAsync(
        long userId,
        int page,
        int pageSize);

    // Landlord xem chi tiết conversation thuộc phòng của mình
    Task<ConversationResponse> GetLandlordConversationByIdAsync(
        long userId,
        long conversationId);
}