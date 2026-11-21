using Roomify.API.DTOs.Admin.Payments;

namespace Roomify.API.Services.Interfaces;

public interface IAdminPaymentService
{
    Task<AdminPaymentListResponse>
        GetPaymentsAsync(
            string? status,
            string? paymentType,
            string? search,
            int page,
            int pageSize);


    Task<AdminPaymentResponse>
        GetPaymentByIdAsync(
            long paymentId);
}