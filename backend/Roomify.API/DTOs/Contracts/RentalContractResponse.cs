namespace Roomify.API.DTOs.Contracts;

public class RentalContractResponse
{
    public long ContractId { get; set; }

    public long RentalRequestId { get; set; }

    public string ContractCode { get; set; } = null!;

    public long RoomId { get; set; }

    public string RoomTitle { get; set; } = null!;

    public long PropertyId { get; set; }

    public string PropertyName { get; set; } = null!;

    public long StudentId { get; set; }

    public string StudentName { get; set; } = null!;

    public string? StudentPhone { get; set; }

    public long LandlordId { get; set; }

    public string LandlordName { get; set; } = null!;

    public DateOnly StartDate { get; set; }

    public DateOnly? EndDate { get; set; }

    public decimal MonthlyRent { get; set; }

    public decimal DepositAmount { get; set; }

    public string? Terms { get; set; }

    public string? ContractFileUrl { get; set; }

    public string Status { get; set; } = null!;

    public DateTime CreatedAt { get; set; }

    public DateTime? ActivatedAt { get; set; }

    public DateTime? CompletedAt { get; set; }

    public DateTime? CancelledAt { get; set; }
}