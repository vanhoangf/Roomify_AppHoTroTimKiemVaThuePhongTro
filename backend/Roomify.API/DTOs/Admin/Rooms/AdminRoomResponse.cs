namespace Roomify.API.DTOs.Admin.Rooms;

public class AdminRoomResponse
{
    public long RoomId { get; set; }

    public long PropertyId { get; set; }

    public string? RoomNumber { get; set; }

    public string Title { get; set; } = string.Empty;

    public string? Description { get; set; }

    public decimal AreaM2 { get; set; }

    public decimal RentPrice { get; set; }

    public decimal? DepositAmount { get; set; }

    public decimal? ElectricityPrice { get; set; }

    public decimal? WaterPrice { get; set; }

    public decimal? InternetPrice { get; set; }

    public decimal? ServiceFee { get; set; }

    public decimal? ParkingFee { get; set; }

    public int MaxOccupants { get; set; }

    public string GenderPreference { get; set; } = string.Empty;

    public DateOnly? AvailableFrom { get; set; }

    public string ListingStatus { get; set; } = string.Empty;

    public string AvailabilityStatus { get; set; } = string.Empty;

    public int ViewCount { get; set; }

    public string PropertyName { get; set; } = string.Empty;

    public string? PropertyDescription { get; set; }

    public int PropertyTypeId { get; set; }

    public string PropertyTypeName { get; set; } = string.Empty;

    public string PropertyStatus { get; set; } = string.Empty;

    public int ProvinceId { get; set; }

    public string ProvinceName { get; set; } = string.Empty;

    public int WardId { get; set; }

    public string WardName { get; set; } = string.Empty;

    public string AddressLine { get; set; } = string.Empty;

    public decimal? Latitude { get; set; }

    public decimal? Longitude { get; set; }

    public long LandlordId { get; set; }

    public long LandlordUserId { get; set; }

    public string LandlordName { get; set; } = string.Empty;

    public string LandlordEmail { get; set; } = string.Empty;

    public string? LandlordPhone { get; set; }

    public bool LandlordIsVerified { get; set; }

    public long? ReviewedByUserId { get; set; }

    public string? ReviewedByUserName { get; set; }

    public DateTime? ReviewedAt { get; set; }

    public string? RejectionReason { get; set; }

    public DateTime? PublishedAt { get; set; }

    public int SavedCount { get; set; }

    public int TotalReports { get; set; }

    public int OpenReports { get; set; }

    public int PropertyTotalRooms { get; set; }

    public int PropertyAvailableRooms { get; set; }

    public int PropertyRentedRooms { get; set; }

    public List<AdminRoomImageResponse> Images { get; set; } = [];

    public List<AdminRoomAmenityResponse> Amenities { get; set; } = [];

    public List<AdminRoomReportResponse> RecentReports { get; set; } = [];

    public DateTime CreatedAt { get; set; }

    public DateTime UpdatedAt { get; set; }
}


public class AdminRoomImageResponse
{
    public long ImageId { get; set; }

    public string ImageUrl { get; set; } = string.Empty;

    public bool IsPrimary { get; set; }

    public int SortOrder { get; set; }
}


public class AdminRoomAmenityResponse
{
    public int AmenityId { get; set; }

    public string AmenityName { get; set; } = string.Empty;

    public string? IconUrl { get; set; }
}


public class AdminRoomReportResponse
{
    public long ReportId { get; set; }

    public long ReporterUserId { get; set; }

    public string ReporterName { get; set; } = string.Empty;

    public string ReporterEmail { get; set; } = string.Empty;

    public string Reason { get; set; } = string.Empty;

    public string? Description { get; set; }

    public string Status { get; set; } = string.Empty;

    public DateTime CreatedAt { get; set; }
}