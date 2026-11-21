namespace Roomify.Admin.Models.Statistics;

public class AdminContractStatisticsDto
{
    public int TotalContracts { get; set; }

    public int PendingContracts { get; set; }

    public int ActiveContracts { get; set; }

    public int CompletedContracts { get; set; }

    public int CancelledContracts { get; set; }

    public int CreatedInPeriod { get; set; }

    public List<AdminStatisticsCountPointDto> ContractsByDate{ get; set; } = [];
}