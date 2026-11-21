using Roomify.API.DTOs.Admin.Contracts;

namespace Roomify.API.Services.Interfaces;

public interface IAdminContractService
{
    Task<AdminContractListResponse>
        GetContractsAsync(
            string? status,
            string? search,
            int page,
            int pageSize);


    Task<AdminContractResponse>
        GetContractByIdAsync(
            long contractId);
}