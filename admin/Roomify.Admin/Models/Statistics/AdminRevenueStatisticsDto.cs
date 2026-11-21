namespace Roomify.Admin.Models.Statistics;

public class AdminRevenueStatisticsDto
{
    public int PaidPayments { get; set; }

    public decimal TotalPaidAmount { get; set; }

    public decimal AveragePaymentAmount { get; set; }

    public List<AdminStatisticsAmountPointDto> RevenueByDate{ get; set; } = [];
}