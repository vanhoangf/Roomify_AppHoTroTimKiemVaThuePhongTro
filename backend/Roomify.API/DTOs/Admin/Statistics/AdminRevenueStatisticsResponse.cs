namespace Roomify.API.DTOs.Admin.Statistics;

public class AdminRevenueStatisticsResponse
{
    public int PaidPayments { get; set; }

    public decimal TotalPaidAmount { get; set; }

    public decimal AveragePaymentAmount { get; set; }

    public List<AdminStatisticsAmountPoint> RevenueByDate { get; set; }
        = [];
}