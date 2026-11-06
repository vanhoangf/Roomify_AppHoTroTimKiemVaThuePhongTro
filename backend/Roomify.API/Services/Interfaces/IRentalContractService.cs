using Roomify.API.DTOs.Contracts;

namespace Roomify.API.Services.Interfaces;

public interface IRentalContractService
{
    // =========================================================
    // LANDLORD
    // =========================================================

    // Tạo hợp đồng từ RentalRequest đã APPROVED
    Task<RentalContractResponse> CreateAsync(
        long userId,
        long rentalRequestId,
        CreateRentalContractRequest request);

    // Danh sách hợp đồng thuộc các phòng của landlord
    Task<RentalContractListResponse> GetLandlordContractsAsync(
        long userId,
        string? status,
        int page,
        int pageSize);

    // Landlord xem chi tiết hợp đồng thuộc phòng của mình
    Task<RentalContractResponse> GetLandlordContractByIdAsync(
        long userId,
        long contractId);

    // Landlord hoàn thành hợp đồng ACTIVE
    Task<RentalContractResponse> CompleteAsync(
        long userId,
        long contractId);

    // Landlord hủy hợp đồng
    Task<RentalContractResponse> CancelByLandlordAsync(
        long userId,
        long contractId);


    // =========================================================
    // STUDENT
    // =========================================================

    // Danh sách hợp đồng của student
    Task<RentalContractListResponse> GetStudentContractsAsync(
        long userId,
        string? status,
        int page,
        int pageSize);

    // Student xem chi tiết hợp đồng của mình
    Task<RentalContractResponse> GetStudentContractByIdAsync(
        long userId,
        long contractId);

    // Student chấp nhận hợp đồng PENDING
    // PENDING -> ACTIVE
    Task<RentalContractResponse> ActivateAsync(
        long userId,
        long contractId);

    // Student hủy hợp đồng
    Task<RentalContractResponse> CancelByStudentAsync(
        long userId,
        long contractId);
}