namespace Roomify.API.DTOs.Admin.Contracts;

public class AdminContractResponse
{
    public long ContractId { get; set; }

    public long RentalRequestId { get; set; }

    public string ContractCode { get; set; }
        = string.Empty;


    public long RoomId { get; set; }

    public string RoomTitle { get; set; }
        = string.Empty;


    public long PropertyId { get; set; }

    public string PropertyName { get; set; }
        = string.Empty;


    public long StudentId { get; set; }

    public long StudentUserId { get; set; }

    public string StudentName { get; set; }
        = string.Empty;

    public string StudentEmail { get; set; }
        = string.Empty;

    public string? StudentPhone { get; set; }


    public long LandlordId { get; set; }

    public long LandlordUserId { get; set; }

    public string LandlordName { get; set; }
        = string.Empty;

    public string LandlordEmail { get; set; }
        = string.Empty;

    public string? LandlordPhone { get; set; }


    public DateOnly StartDate { get; set; }

    public DateOnly? EndDate { get; set; }


    public decimal MonthlyRent { get; set; }

    public decimal DepositAmount { get; set; }


    public string? Terms { get; set; }

    public string? ContractFileUrl { get; set; }


    public string Status { get; set; }
        = string.Empty;


    public DateTime CreatedAt { get; set; }

    public DateTime? ActivatedAt { get; set; }

    public DateTime? CompletedAt { get; set; }

    public DateTime? CancelledAt { get; set; }
}