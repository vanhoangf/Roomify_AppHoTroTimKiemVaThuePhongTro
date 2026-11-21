namespace Roomify.Admin.Models.Payments;

public class AdminPaymentDetailViewModel
{
    public AdminPaymentDto? Payment { get; set; }

    public string? ErrorMessage { get; set; }

    public string? ReturnUrl { get; set; }


    public bool HasError =>
        !string.IsNullOrWhiteSpace(
            ErrorMessage);


    public bool IsPending =>
        string.Equals(
            Payment?.Status,
            "PENDING",
            StringComparison.OrdinalIgnoreCase);


    public bool IsPaid =>
        string.Equals(
            Payment?.Status,
            "PAID",
            StringComparison.OrdinalIgnoreCase);


    public bool IsFailed =>
        string.Equals(
            Payment?.Status,
            "FAILED",
            StringComparison.OrdinalIgnoreCase);


    public bool IsCancelled =>
        string.Equals(
            Payment?.Status,
            "CANCELLED",
            StringComparison.OrdinalIgnoreCase);
}