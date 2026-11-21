using Roomify.Admin.Models.Common;
using Roomify.Admin.Models.Payments;

namespace Roomify.Admin.Services.Payments;

public interface IAdminPaymentApiService
{
    Task<ApiResult<AdminPaymentListDto>>
        GetPaymentsAsync(
            string? status = null,
            string? paymentType = null,
            string? search = null,
            int page = 1,
            int pageSize = 10,
            CancellationToken cancellationToken = default);


    Task<ApiResult<AdminPaymentDto>>
        GetPaymentByIdAsync(
            long paymentId,
            CancellationToken cancellationToken = default);
}