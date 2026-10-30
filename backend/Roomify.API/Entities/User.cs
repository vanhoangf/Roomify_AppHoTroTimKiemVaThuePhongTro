using System;
using System.Collections.Generic;

namespace Roomify.API.Entities;

public partial class User
{
    public long UserId { get; set; }

    public int RoleId { get; set; }

    public string Email { get; set; } = null!;

    public string PasswordHash { get; set; } = null!;

    public string FullName { get; set; } = null!;

    public string? Phone { get; set; }

    public string? AvatarUrl { get; set; }

    public string Status { get; set; } = null!;

    public bool EmailVerified { get; set; }

    public string? LockedReason { get; set; }

    public DateTime? LockedAt { get; set; }

    public long? LockedByUserId { get; set; }

    public DateTime? LastLoginAt { get; set; }

    public DateTime CreatedAt { get; set; }

    public DateTime UpdatedAt { get; set; }

    public virtual ICollection<User> InverseLockedByUser { get; set; } = new List<User>();

    public virtual LandlordProfile? LandlordProfileUser { get; set; }

    public virtual ICollection<LandlordProfile> LandlordProfileVerifiedByUsers { get; set; } = new List<LandlordProfile>();

    public virtual User? LockedByUser { get; set; }

    public virtual ICollection<Message> Messages { get; set; } = new List<Message>();

    public virtual ICollection<Notification> Notifications { get; set; } = new List<Notification>();

    public virtual ICollection<PasswordResetToken> PasswordResetTokens { get; set; } = new List<PasswordResetToken>();

    public virtual ICollection<Payment> Payments { get; set; } = new List<Payment>();

    public virtual ICollection<RefreshToken> RefreshTokens { get; set; } = new List<RefreshToken>();

    public virtual ICollection<Report> ReportReporterUsers { get; set; } = new List<Report>();

    public virtual ICollection<Report> ReportResolvedByUsers { get; set; } = new List<Report>();

    public virtual ICollection<Review> Reviews { get; set; } = new List<Review>();

    public virtual Role Role { get; set; } = null!;

    public virtual ICollection<Room> Rooms { get; set; } = new List<Room>();

    public virtual StudentProfile? StudentProfileUser { get; set; }

    public virtual ICollection<StudentProfile> StudentProfileVerifiedByUsers { get; set; } = new List<StudentProfile>();

    public virtual ICollection<UserDeviceToken> UserDeviceTokens { get; set; } = new List<UserDeviceToken>();
}
