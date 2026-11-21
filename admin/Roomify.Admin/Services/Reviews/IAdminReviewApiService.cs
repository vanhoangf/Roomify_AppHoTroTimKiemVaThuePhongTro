using Roomify.Admin.Models.Common;
using Roomify.Admin.Models.Reviews;

namespace Roomify.Admin.Services.Reviews;

public interface IAdminReviewApiService
{
    Task<ApiResult<AdminReviewListDto>>
        GetReviewsAsync(
            string? status = null,
            int? rating = null,
            string? search = null,
            int page = 1,
            int pageSize = 10,
            CancellationToken cancellationToken = default);


    Task<ApiResult<AdminReviewDto>>
        GetReviewByIdAsync(
            long reviewId,
            CancellationToken cancellationToken = default);


    Task<ApiResult<AdminReviewDto>>
        HideReviewAsync(
            long reviewId,
            CancellationToken cancellationToken = default);


    Task<ApiResult<AdminReviewDto>>
        ShowReviewAsync(
            long reviewId,
            CancellationToken cancellationToken = default);
}