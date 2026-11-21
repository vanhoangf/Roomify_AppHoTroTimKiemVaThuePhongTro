namespace Roomify.Admin.Models.Payments;

public class AdminPaymentListDto
{
    public List<AdminPaymentDto> Items { get; set; }
        = [];


    public int TotalCount { get; set; }


    public int TotalPayments { get; set; }

    public int PendingPayments { get; set; }

    public int PaidPayments { get; set; }

    public int FailedPayments { get; set; }

    public int CancelledPayments { get; set; }

    public decimal TotalPaidAmount { get; set; }


    public int Page { get; set; }

    public int PageSize { get; set; }

    public int TotalPages { get; set; }

    public bool HasNextPage { get; set; }

    public bool HasPreviousPage { get; set; }
}