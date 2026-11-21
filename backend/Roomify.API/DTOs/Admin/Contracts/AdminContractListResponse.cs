namespace Roomify.API.DTOs.Admin.Contracts;

public class AdminContractListResponse
{
    public List<AdminContractResponse> Items { get; set; } = [];

    public int TotalCount { get; set; }

    public int TotalContracts { get; set; }

    public int PendingContracts { get; set; }

    public int ActiveContracts { get; set; }

    public int CompletedContracts { get; set; }

    public int CancelledContracts { get; set; }


    public int Page { get; set; }

    public int PageSize { get; set; }

    public int TotalPages { get; set; }

    public bool HasNextPage { get; set; }

    public bool HasPreviousPage { get; set; }
}