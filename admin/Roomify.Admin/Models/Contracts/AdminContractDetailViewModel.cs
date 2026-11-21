namespace Roomify.Admin.Models.Contracts;

public class AdminContractDetailViewModel
{
    public AdminContractDto? Contract { get; set; }

    public string? ErrorMessage { get; set; }

    public string? ReturnUrl { get; set; }


    public bool HasError =>
        !string.IsNullOrWhiteSpace(
            ErrorMessage);


    public bool IsPending =>
        string.Equals(
            Contract?.Status,
            "PENDING",
            StringComparison.OrdinalIgnoreCase);


    public bool IsActive =>
        string.Equals(
            Contract?.Status,
            "ACTIVE",
            StringComparison.OrdinalIgnoreCase);


    public bool IsCompleted =>
        string.Equals(
            Contract?.Status,
            "COMPLETED",
            StringComparison.OrdinalIgnoreCase);


    public bool IsCancelled =>
        string.Equals(
            Contract?.Status,
            "CANCELLED",
            StringComparison.OrdinalIgnoreCase);
}