namespace Roomify.Admin.Models.Payments;

public class AdminPaymentsViewModel
{
    public AdminPaymentListDto? Payments { get; set; }

    public string? ErrorMessage { get; set; }

    public string? Search { get; set; }

    public string? Status { get; set; }

    public string? PaymentType { get; set; }

    public int Page { get; set; } = 1;

    public int PageSize { get; set; } = 10;


    public bool HasError =>
        !string.IsNullOrWhiteSpace(
            ErrorMessage);


    public bool IsEmpty =>
        Payments is not null &&
        Payments.Items.Count == 0;
}