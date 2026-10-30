using System;
using System.Collections.Generic;

namespace Roomify.API.Entities;

public partial class StudentProfile
{
    public long StudentId { get; set; }

    public long UserId { get; set; }

    public long? UniversityId { get; set; }

    public string? StudentCode { get; set; }

    public string? StudentCardUrl { get; set; }

    public bool IsVerified { get; set; }

    public long? VerifiedByUserId { get; set; }

    public DateTime? VerifiedAt { get; set; }

    public DateTime CreatedAt { get; set; }

    public DateTime UpdatedAt { get; set; }

    public virtual ICollection<Conversation> Conversations { get; set; } = new List<Conversation>();

    public virtual ICollection<RentalRequest> RentalRequests { get; set; } = new List<RentalRequest>();

    public virtual ICollection<RoomViewHistory> RoomViewHistories { get; set; } = new List<RoomViewHistory>();

    public virtual ICollection<SavedRoom> SavedRooms { get; set; } = new List<SavedRoom>();

    public virtual University? University { get; set; }

    public virtual User User { get; set; } = null!;

    public virtual User? VerifiedByUser { get; set; }

    public virtual ICollection<ViewingAppointment> ViewingAppointments { get; set; } = new List<ViewingAppointment>();
}
