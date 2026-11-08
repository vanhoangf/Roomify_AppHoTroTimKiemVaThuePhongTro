using Roomify.API.DTOs.Reviews;

namespace Roomify.API.Services.Interfaces;

public interface IReviewService
{
    // STUDENT
    // Sinh viên tạo đánh giá từ hợp đồng thuê của chính mình
    Task<ReviewResponse> CreateAsync(
        long userId,
        long contractId,
        CreateReviewRequest request);

    // Sinh viên xem review đã tạo theo Contract
    Task<ReviewResponse> GetStudentReviewByContractAsync(
        long userId,
        long contractId);
    // PUBLIC
    // Danh sách review VISIBLE của một phòng
    Task<ReviewListResponse> GetRoomReviewsAsync(
        long roomId,
        int page,
        int pageSize);
}