using System;
using System.Collections.Generic;

namespace Roomify.API.Entities;

public partial class Room
{
    public long RoomId { get; set; }

    public long PropertyId { get; set; }

    public string? RoomNumber { get; set; }

    public string Title { get; set; } = null!;

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

    public string GenderPreference { get; set; } = null!;

    public DateOnly? AvailableFrom { get; set; }

    public string ListingStatus { get; set; } = null!;

    public string AvailabilityStatus { get; set; } = null!;

    public int ViewCount { get; set; }

    public long? ReviewedByUserId { get; set; }

    public DateTime? ReviewedAt { get; set; }

    public string? RejectionReason { get; set; }

    public DateTime? PublishedAt { get; set; }

    public DateTime CreatedAt { get; set; }

    public DateTime UpdatedAt { get; set; }

    public virtual ICollection<Conversation> Conversations { get; set; } = new List<Conversation>();

    public virtual Property Property { get; set; } = null!;

    public virtual ICollection<RentalRequest> RentalRequests { get; set; } = new List<RentalRequest>();

    public virtual ICollection<Report> Reports { get; set; } = new List<Report>();

    public virtual User? ReviewedByUser { get; set; }

    public virtual ICollection<RoomImage> RoomImages { get; set; } = new List<RoomImage>();

    public virtual ICollection<RoomViewHistory> RoomViewHistories { get; set; } = new List<RoomViewHistory>();

    public virtual ICollection<SavedRoom> SavedRooms { get; set; } = new List<SavedRoom>();

    public virtual ICollection<ViewingAppointment> ViewingAppointments { get; set; } = new List<ViewingAppointment>();

    public virtual ICollection<Amenity> Amenities { get; set; } = new List<Amenity>();
}
