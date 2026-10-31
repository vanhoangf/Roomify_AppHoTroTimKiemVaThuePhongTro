using System.ComponentModel.DataAnnotations;

namespace Roomify.API.DTOs.Landlord;

public class CreateRoomRequest
{
    [StringLength(50)]
    public string? RoomNumber { get; set; }

    [Required]
    [StringLength(255)]
    public string Title { get; set; } = string.Empty;

    public string? Description { get; set; }

    [Range(0.01, double.MaxValue)]
    public decimal AreaM2 { get; set; }

    [Range(0, double.MaxValue)]
    public decimal RentPrice { get; set; }

    [Range(0, double.MaxValue)]
    public decimal? DepositAmount { get; set; }

    [Range(0, double.MaxValue)]
    public decimal? ElectricityPrice { get; set; }

    [Range(0, double.MaxValue)]
    public decimal? WaterPrice { get; set; }

    [Range(0, double.MaxValue)]
    public decimal? InternetPrice { get; set; }

    [Range(0, double.MaxValue)]
    public decimal? ServiceFee { get; set; }

    [Range(0, double.MaxValue)]
    public decimal? ParkingFee { get; set; }

    [Range(1, int.MaxValue)]
    public int MaxOccupants { get; set; } = 1;

    [Required]
    public string GenderPreference { get; set; } = "ALL";

    public DateOnly? AvailableFrom { get; set; }

    public List<int> AmenityIds { get; set; } = [];
}