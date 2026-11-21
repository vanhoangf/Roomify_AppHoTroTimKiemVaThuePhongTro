namespace Roomify.Admin.Models.Contracts;

public class AdminContractsViewModel
{
    public AdminContractListDto? Contracts { get; set; }

    public string? ErrorMessage { get; set; }

    public string? Search { get; set; }

    public string? Status { get; set; }

    public int Page { get; set; } = 1;

    public int PageSize { get; set; } = 10;


    public bool HasError =>
        !string.IsNullOrWhiteSpace(
            ErrorMessage);


    public bool IsEmpty =>
        Contracts is not null &&
        Contracts.Items.Count == 0;
}