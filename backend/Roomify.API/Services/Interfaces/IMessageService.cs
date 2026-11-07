using Roomify.API.DTOs.Messaging;

namespace Roomify.API.Services.Interfaces;

public interface IMessageService
{
    // STUDENT
    // Student lấy lịch sử tin nhắn trong conversation của mình
    Task<List<MessageResponse>> GetStudentMessagesAsync(
        long userId,
        long conversationId);

    // Student gửi tin nhắn
    Task<MessageResponse> SendStudentMessageAsync(
        long userId,
        long conversationId,
        SendMessageRequest request);

    // Student đánh dấu các tin Landlord gửi là đã đọc
    Task<int> MarkStudentConversationReadAsync(
        long userId,
        long conversationId);

    // LANDLORD
    // Landlord lấy lịch sử tin nhắn của conversation thuộc phòng mình
    Task<List<MessageResponse>> GetLandlordMessagesAsync(
        long userId,
        long conversationId);

    // Landlord gửi tin nhắn
    Task<MessageResponse> SendLandlordMessageAsync(
        long userId,
        long conversationId,
        SendMessageRequest request);

    // Landlord đánh dấu tin Student gửi là đã đọc
    Task<int> MarkLandlordConversationReadAsync(
        long userId,
        long conversationId);
}