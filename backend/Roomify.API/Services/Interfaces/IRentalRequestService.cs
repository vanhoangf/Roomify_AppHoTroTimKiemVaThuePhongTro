using Roomify.API.DTOs.Rentals;

namespace Roomify.API.Services.Interfaces;

public interface IRentalRequestService
{
    Task<RentalRequestResponse> CreateAsync(
        long userId,
        long roomId,
        CreateRentalRequestRequest request);

    Task<RentalRequestListResponse> GetStudentListAsync(
        long userId,
        string? status,
        int page,
        int pageSize);

    Task<RentalRequestResponse> GetStudentDetailAsync(
        long userId,
        long rentalRequestId);

    Task<RentalRequestResponse> CancelByStudentAsync(
        long userId,
        long rentalRequestId);

    Task<RentalRequestListResponse> GetLandlordListAsync(
        long userId,
        string? status,
        int page,
        int pageSize);

    Task<RentalRequestResponse> GetLandlordDetailAsync(
        long userId,
        long rentalRequestId);

    Task<RentalRequestResponse> ApproveAsync(
        long userId,
        long rentalRequestId,
        RentalRequestDecisionRequest request);

    Task<RentalRequestResponse> RejectAsync(
        long userId,
        long rentalRequestId,
        RentalRequestDecisionRequest request);
}