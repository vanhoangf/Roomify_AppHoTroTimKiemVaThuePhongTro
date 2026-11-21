using Roomify.Admin.Models.Common;
using Roomify.Admin.Models.Contracts;

namespace Roomify.Admin.Services.Contracts;

public interface IAdminContractApiService
{
    Task<ApiResult<AdminContractListDto>>
        GetContractsAsync(
            string? status = null,
            string? search = null,
            int page = 1,
            int pageSize = 10,
            CancellationToken cancellationToken = default);


    Task<ApiResult<AdminContractDto>>
        GetContractByIdAsync(
            long contractId,
            CancellationToken cancellationToken = default);
}