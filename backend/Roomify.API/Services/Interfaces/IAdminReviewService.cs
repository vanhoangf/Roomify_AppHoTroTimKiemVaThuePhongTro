using Roomify.API.DTOs.Admin.Reviews;

namespace Roomify.API.Services.Interfaces;

public interface IAdminReviewService
{
    Task<AdminReviewListResponse> GetReviewsAsync(
        string? status,
        int? rating,
        string? search,
        int page,
        int pageSize);


    Task<AdminReviewResponse> GetReviewByIdAsync(
        long reviewId);


    Task<AdminReviewResponse> HideAsync(
        long adminUserId,
        long reviewId);


    Task<AdminReviewResponse> ShowAsync(
        long adminUserId,
        long reviewId);
}