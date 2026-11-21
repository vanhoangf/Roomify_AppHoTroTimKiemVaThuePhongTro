namespace Roomify.API.DTOs.Admin.Payments;

public class AdminPaymentListResponse
{
    public List<AdminPaymentResponse> Items { get; set; }
        = [];


    // Số bản ghi sau filter
    public int TotalCount { get; set; }


    // GLOBAL STATISTICS
    public int TotalPayments { get; set; }

    public int PendingPayments { get; set; }

    public int PaidPayments { get; set; }

    public int FailedPayments { get; set; }

    public int CancelledPayments { get; set; }

    public decimal TotalPaidAmount { get; set; }


    // PAGINATION
    public int Page { get; set; }

    public int PageSize { get; set; }

    public int TotalPages { get; set; }

    public bool HasNextPage { get; set; }

    public bool HasPreviousPage { get; set; }
}