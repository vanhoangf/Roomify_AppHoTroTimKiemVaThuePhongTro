using System.ComponentModel.DataAnnotations;

namespace Roomify.API.DTOs.Contracts;

public class CreateRentalContractRequest
{
    [MaxLength(3000)]
    public string? Terms { get; set; }

    [MaxLength(2000)]
    public string? ContractFileUrl { get; set; }
}