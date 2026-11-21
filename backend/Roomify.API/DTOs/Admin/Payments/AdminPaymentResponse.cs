namespace Roomify.API.DTOs.Admin.Payments;

public class AdminPaymentResponse
{
    public long PaymentId { get; set; }


    // CONTRACT
    public long ContractId { get; set; }

    public string ContractCode { get; set; }
        = string.Empty;


    // ROOM / PROPERTY
    public long RoomId { get; set; }

    public string RoomTitle { get; set; }
        = string.Empty;

    public long PropertyId { get; set; }

    public string PropertyName { get; set; }
        = string.Empty;


    // STUDENT
    public long StudentId { get; set; }

    public long StudentUserId { get; set; }

    public string StudentName { get; set; }
        = string.Empty;

    public string StudentEmail { get; set; }
        = string.Empty;

    public string? StudentPhone { get; set; }


    // LANDLORD
    public long LandlordId { get; set; }

    public long LandlordUserId { get; set; }

    public string LandlordName { get; set; }
        = string.Empty;

    public string LandlordEmail { get; set; }
        = string.Empty;

    public string? LandlordPhone { get; set; }


    // PAYMENT
    public string PaymentType { get; set; }
        = string.Empty;

    public decimal Amount { get; set; }

    public DateOnly? DueDate { get; set; }

    public string? PaymentMethod { get; set; }

    public string Status { get; set; }
        = string.Empty;


    // PAYMENT INFORMATION
    public string? TransactionReference { get; set; }

    public string? ProofImageUrl { get; set; }

    public string? Note { get; set; }


    // CONFIRMATION
    public long? ConfirmedByUserId { get; set; }

    public string? ConfirmedByUserName { get; set; }

    public DateTime? ConfirmedAt { get; set; }

    public DateTime? PaidAt { get; set; }


    // TIMESTAMPS
    public DateTime CreatedAt { get; set; }

    public DateTime UpdatedAt { get; set; }
}