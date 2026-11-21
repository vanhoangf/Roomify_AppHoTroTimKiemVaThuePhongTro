namespace Roomify.API.DTOs.Admin.Statistics;

public class AdminContractStatisticsResponse
{
    public int TotalContracts { get; set; }

    public int PendingContracts { get; set; }

    public int ActiveContracts { get; set; }

    public int CompletedContracts { get; set; }

    public int CancelledContracts { get; set; }

    public int CreatedInPeriod { get; set; }

    public List<AdminStatisticsCountPoint> ContractsByDate { get; set; }
        = [];
}