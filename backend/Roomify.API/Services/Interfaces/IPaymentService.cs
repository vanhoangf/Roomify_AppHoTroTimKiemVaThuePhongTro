using Roomify.API.DTOs.Payments;

namespace Roomify.API.Services.Interfaces;

public interface IPaymentService
{
    // ============================================================
    // LANDLORD
    // ============================================================

    // Landlord tạo khoản thanh toán cho hợp đồng thuộc phòng của mình
    Task<PaymentResponse> CreateAsync(
        long userId,
        long contractId,
        CreatePaymentRequest request);

    // Landlord xem danh sách payment thuộc các hợp đồng/phòng của mình
    Task<PaymentListResponse> GetLandlordPaymentsAsync(
        long userId,
        string? status,
        string? paymentType,
        int page,
        int pageSize);

    // Landlord xem chi tiết payment thuộc phòng của mình
    Task<PaymentResponse> GetLandlordPaymentByIdAsync(
        long userId,
        long paymentId);

    // Landlord xác nhận payment
    Task<PaymentResponse> ConfirmAsync(
        long userId,
        long paymentId,
        ConfirmPaymentRequest request);

    // Landlord từ chối payment
    Task<PaymentResponse> RejectAsync(
        long userId,
        long paymentId,
        RejectPaymentRequest request);

    // Landlord hủy khoản thanh toán
    Task<PaymentResponse> CancelByLandlordAsync(
        long userId,
        long paymentId);


    // ============================================================
    // STUDENT
    // ============================================================

    // Student xem danh sách payment thuộc hợp đồng của mình
    Task<PaymentListResponse> GetStudentPaymentsAsync(
        long userId,
        string? status,
        string? paymentType,
        int page,
        int pageSize);

    // Student xem chi tiết payment của mình
    Task<PaymentResponse> GetStudentPaymentByIdAsync(
        long userId,
        long paymentId);

    // Student gửi thông tin thanh toán / minh chứng
    Task<PaymentResponse> SubmitAsync(
        long userId,
        long paymentId,
        SubmitPaymentRequest request);

    // Student hủy payment đang chờ xử lý
    Task<PaymentResponse> CancelByStudentAsync(
        long userId,
        long paymentId);
}