using System;
using System.Collections.Generic;
using Microsoft.EntityFrameworkCore;
using Roomify.API.Entities;

namespace Roomify.API.Data;

public partial class RoomifyDbContext : DbContext
{
    public RoomifyDbContext(DbContextOptions<RoomifyDbContext> options)
        : base(options)
    {
    }

    public virtual DbSet<Amenity> Amenities { get; set; }

    public virtual DbSet<Conversation> Conversations { get; set; }

    public virtual DbSet<LandlordProfile> LandlordProfiles { get; set; }

    public virtual DbSet<Message> Messages { get; set; }

    public virtual DbSet<Notification> Notifications { get; set; }

    public virtual DbSet<PasswordResetToken> PasswordResetTokens { get; set; }

    public virtual DbSet<Payment> Payments { get; set; }

    public virtual DbSet<Property> Properties { get; set; }

    public virtual DbSet<PropertyType> PropertyTypes { get; set; }

    public virtual DbSet<Province> Provinces { get; set; }

    public virtual DbSet<RefreshToken> RefreshTokens { get; set; }

    public virtual DbSet<RentalContract> RentalContracts { get; set; }

    public virtual DbSet<RentalRequest> RentalRequests { get; set; }

    public virtual DbSet<Report> Reports { get; set; }

    public virtual DbSet<Review> Reviews { get; set; }

    public virtual DbSet<Role> Roles { get; set; }

    public virtual DbSet<Room> Rooms { get; set; }

    public virtual DbSet<RoomImage> RoomImages { get; set; }

    public virtual DbSet<RoomViewHistory> RoomViewHistories { get; set; }

    public virtual DbSet<SavedRoom> SavedRooms { get; set; }

    public virtual DbSet<StudentProfile> StudentProfiles { get; set; }

    public virtual DbSet<University> Universities { get; set; }

    public virtual DbSet<User> Users { get; set; }

    public virtual DbSet<UserDeviceToken> UserDeviceTokens { get; set; }

    public virtual DbSet<ViewingAppointment> ViewingAppointments { get; set; }

    public virtual DbSet<VwRentalContractDetail> VwRentalContractDetails { get; set; }

    public virtual DbSet<Ward> Wards { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<Amenity>(entity =>
        {
            entity.HasKey(e => e.AmenityId).HasName("PK__Amenitie__842AF52BE9D31B36");

            entity.HasIndex(e => e.AmenityName, "UQ_Amenities_AmenityName").IsUnique();

            entity.Property(e => e.AmenityId).HasColumnName("AmenityID");
            entity.Property(e => e.AmenityName).HasMaxLength(100);
            entity.Property(e => e.IconUrl)
                .HasMaxLength(500)
                .HasColumnName("IconURL");
            entity.Property(e => e.IsActive).HasDefaultValue(true, "DF_Amenities_IsActive");
        });

        modelBuilder.Entity<Conversation>(entity =>
        {
            entity.HasKey(e => e.ConversationId).HasName("PK__Conversa__C050D8978FD10129");

            entity.HasIndex(e => new { e.RoomId, e.LastMessageAt }, "IX_Conversations_RoomID").IsDescending(false, true);

            entity.HasIndex(e => new { e.StudentId, e.LastMessageAt }, "IX_Conversations_StudentID").IsDescending(false, true);

            entity.HasIndex(e => new { e.RoomId, e.StudentId }, "UQ_Conversations_Room_Student").IsUnique();

            entity.Property(e => e.ConversationId).HasColumnName("ConversationID");
            entity.Property(e => e.CreatedAt).HasDefaultValueSql("(sysutcdatetime())", "DF_Conversations_CreatedAt");
            entity.Property(e => e.RoomId).HasColumnName("RoomID");
            entity.Property(e => e.StudentId).HasColumnName("StudentID");

            entity.HasOne(d => d.Room).WithMany(p => p.Conversations)
                .HasForeignKey(d => d.RoomId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_Conversations_Rooms");

            entity.HasOne(d => d.Student).WithMany(p => p.Conversations)
                .HasForeignKey(d => d.StudentId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_Conversations_Students");
        });

        modelBuilder.Entity<LandlordProfile>(entity =>
        {
            entity.HasKey(e => e.LandlordId).HasName("PK__Landlord__8EC7924371004A2A");

            entity.HasIndex(e => e.UserId, "UQ_LandlordProfiles_UserID").IsUnique();

            entity.HasIndex(e => e.IdentityNumber, "UX_LandlordProfiles_IdentityNumber_NotNull")
                .IsUnique()
                .HasFilter("([IdentityNumber] IS NOT NULL)");

            entity.Property(e => e.LandlordId).HasColumnName("LandlordID");
            entity.Property(e => e.CreatedAt).HasDefaultValueSql("(sysutcdatetime())", "DF_LandlordProfiles_CreatedAt");
            entity.Property(e => e.IdentityDocumentUrl)
                .HasMaxLength(1000)
                .HasColumnName("IdentityDocumentURL");
            entity.Property(e => e.IdentityNumber)
                .HasMaxLength(30)
                .IsUnicode(false);
            entity.Property(e => e.UpdatedAt).HasDefaultValueSql("(sysutcdatetime())", "DF_LandlordProfiles_UpdatedAt");
            entity.Property(e => e.UserId).HasColumnName("UserID");
            entity.Property(e => e.VerifiedByUserId).HasColumnName("VerifiedByUserID");

            entity.HasOne(d => d.User).WithOne(p => p.LandlordProfileUser)
                .HasForeignKey<LandlordProfile>(d => d.UserId)
                .HasConstraintName("FK_LandlordProfiles_Users");

            entity.HasOne(d => d.VerifiedByUser).WithMany(p => p.LandlordProfileVerifiedByUsers)
                .HasForeignKey(d => d.VerifiedByUserId)
                .HasConstraintName("FK_LandlordProfiles_VerifiedBy");
        });

        modelBuilder.Entity<Message>(entity =>
        {
            entity.HasKey(e => e.MessageId).HasName("PK__Messages__C87C037C3F5923C3");

            entity.HasIndex(e => new { e.ConversationId, e.SentAt }, "IX_Messages_ConversationID_SentAt").IsDescending(false, true);

            entity.HasIndex(e => new { e.ConversationId, e.IsRead, e.SentAt }, "IX_Messages_Unread").IsDescending(false, false, true);

            entity.Property(e => e.MessageId).HasColumnName("MessageID");
            entity.Property(e => e.Content).HasMaxLength(2000);
            entity.Property(e => e.ConversationId).HasColumnName("ConversationID");
            entity.Property(e => e.ImageUrl)
                .HasMaxLength(1000)
                .HasColumnName("ImageURL");
            entity.Property(e => e.MessageType)
                .HasMaxLength(20)
                .IsUnicode(false)
                .HasDefaultValue("TEXT", "DF_Messages_MessageType");
            entity.Property(e => e.SenderUserId).HasColumnName("SenderUserID");
            entity.Property(e => e.SentAt).HasDefaultValueSql("(sysutcdatetime())", "DF_Messages_SentAt");

            entity.HasOne(d => d.Conversation).WithMany(p => p.Messages)
                .HasForeignKey(d => d.ConversationId)
                .HasConstraintName("FK_Messages_Conversations");

            entity.HasOne(d => d.SenderUser).WithMany(p => p.Messages)
                .HasForeignKey(d => d.SenderUserId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_Messages_Sender");
        });

        modelBuilder.Entity<Notification>(entity =>
        {
            entity.HasKey(e => e.NotificationId).HasName("PK__Notifica__20CF2E32A797B51E");

            entity.HasIndex(e => new { e.UserId, e.IsRead, e.CreatedAt }, "IX_Notifications_UserID_IsRead").IsDescending(false, false, true);

            entity.Property(e => e.NotificationId).HasColumnName("NotificationID");
            entity.Property(e => e.CreatedAt).HasDefaultValueSql("(sysutcdatetime())", "DF_Notifications_CreatedAt");
            entity.Property(e => e.Message).HasMaxLength(1000);
            entity.Property(e => e.NotificationType)
                .HasMaxLength(50)
                .IsUnicode(false);
            entity.Property(e => e.RelatedEntityId).HasColumnName("RelatedEntityID");
            entity.Property(e => e.RelatedEntityType)
                .HasMaxLength(50)
                .IsUnicode(false);
            entity.Property(e => e.Title).HasMaxLength(255);
            entity.Property(e => e.UserId).HasColumnName("UserID");

            entity.HasOne(d => d.User).WithMany(p => p.Notifications)
                .HasForeignKey(d => d.UserId)
                .HasConstraintName("FK_Notifications_Users");
        });

        modelBuilder.Entity<PasswordResetToken>(entity =>
        {
            entity.HasKey(e => e.PasswordResetTokenId).HasName("PK__Password__16065EC882B2C647");

            entity.HasIndex(e => e.ExpiresAt, "IX_PasswordResetTokens_ExpiresAt");

            entity.HasIndex(e => e.UserId, "IX_PasswordResetTokens_UserID");

            entity.HasIndex(e => e.TokenHash, "UQ_PasswordResetTokens_TokenHash").IsUnique();

            entity.Property(e => e.PasswordResetTokenId).HasColumnName("PasswordResetTokenID");
            entity.Property(e => e.CreatedAt).HasDefaultValueSql("(sysutcdatetime())", "DF_PasswordResetTokens_CreatedAt");
            entity.Property(e => e.TokenHash)
                .HasMaxLength(500)
                .IsUnicode(false);
            entity.Property(e => e.UserId).HasColumnName("UserID");

            entity.HasOne(d => d.User).WithMany(p => p.PasswordResetTokens)
                .HasForeignKey(d => d.UserId)
                .HasConstraintName("FK_PasswordResetTokens_Users");
        });

        modelBuilder.Entity<Payment>(entity =>
        {
            entity.HasKey(e => e.PaymentId).HasName("PK__Payments__9B556A580673B8FF");

            entity.HasIndex(e => new { e.ContractId, e.CreatedAt }, "IX_Payments_ContractID").IsDescending(false, true);

            entity.HasIndex(e => e.PaidAt, "IX_Payments_PaidAt").IsDescending();

            entity.HasIndex(e => new { e.Status, e.DueDate }, "IX_Payments_Status_DueDate");

            entity.Property(e => e.PaymentId).HasColumnName("PaymentID");
            entity.Property(e => e.Amount).HasColumnType("decimal(18, 2)");
            entity.Property(e => e.ConfirmedByUserId).HasColumnName("ConfirmedByUserID");
            entity.Property(e => e.ContractId).HasColumnName("ContractID");
            entity.Property(e => e.CreatedAt).HasDefaultValueSql("(sysutcdatetime())", "DF_Payments_CreatedAt");
            entity.Property(e => e.Note).HasMaxLength(1000);
            entity.Property(e => e.PaymentMethod)
                .HasMaxLength(30)
                .IsUnicode(false);
            entity.Property(e => e.PaymentType)
                .HasMaxLength(20)
                .IsUnicode(false);
            entity.Property(e => e.ProofImageUrl)
                .HasMaxLength(1000)
                .HasColumnName("ProofImageURL");
            entity.Property(e => e.Status)
                .HasMaxLength(20)
                .IsUnicode(false)
                .HasDefaultValue("PENDING", "DF_Payments_Status");
            entity.Property(e => e.TransactionReference)
                .HasMaxLength(150)
                .IsUnicode(false);
            entity.Property(e => e.UpdatedAt).HasDefaultValueSql("(sysutcdatetime())", "DF_Payments_UpdatedAt");

            entity.HasOne(d => d.ConfirmedByUser).WithMany(p => p.Payments)
                .HasForeignKey(d => d.ConfirmedByUserId)
                .HasConstraintName("FK_Payments_ConfirmedBy");

            entity.HasOne(d => d.Contract).WithMany(p => p.Payments)
                .HasForeignKey(d => d.ContractId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_Payments_Contracts");
        });

        modelBuilder.Entity<Property>(entity =>
        {
            entity.HasKey(e => e.PropertyId).HasName("PK__Properti__70C9A7551E77E878");

            entity.HasIndex(e => e.LandlordId, "IX_Properties_LandlordID");

            entity.HasIndex(e => new { e.ProvinceId, e.WardId }, "IX_Properties_Location");

            entity.HasIndex(e => e.PropertyTypeId, "IX_Properties_PropertyTypeID");

            entity.HasIndex(e => e.Status, "IX_Properties_Status");

            entity.Property(e => e.PropertyId).HasColumnName("PropertyID");
            entity.Property(e => e.AddressLine).HasMaxLength(500);
            entity.Property(e => e.CreatedAt).HasDefaultValueSql("(sysutcdatetime())", "DF_Properties_CreatedAt");
            entity.Property(e => e.Description).HasMaxLength(2000);
            entity.Property(e => e.LandlordId).HasColumnName("LandlordID");
            entity.Property(e => e.Latitude).HasColumnType("decimal(10, 7)");
            entity.Property(e => e.Longitude).HasColumnType("decimal(10, 7)");
            entity.Property(e => e.PropertyName).HasMaxLength(255);
            entity.Property(e => e.PropertyTypeId).HasColumnName("PropertyTypeID");
            entity.Property(e => e.ProvinceId).HasColumnName("ProvinceID");
            entity.Property(e => e.Status)
                .HasMaxLength(20)
                .IsUnicode(false)
                .HasDefaultValue("ACTIVE", "DF_Properties_Status");
            entity.Property(e => e.UpdatedAt).HasDefaultValueSql("(sysutcdatetime())", "DF_Properties_UpdatedAt");
            entity.Property(e => e.WardId).HasColumnName("WardID");

            entity.HasOne(d => d.Landlord).WithMany(p => p.Properties)
                .HasForeignKey(d => d.LandlordId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_Properties_Landlords");

            entity.HasOne(d => d.PropertyType).WithMany(p => p.Properties)
                .HasForeignKey(d => d.PropertyTypeId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_Properties_PropertyTypes");

            entity.HasOne(d => d.Province).WithMany(p => p.Properties)
                .HasForeignKey(d => d.ProvinceId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_Properties_Provinces");

            entity.HasOne(d => d.Ward).WithMany(p => p.Properties)
                .HasPrincipalKey(p => new { p.ProvinceId, p.WardId })
                .HasForeignKey(d => new { d.ProvinceId, d.WardId })
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_Properties_ProvinceWard");
        });

        modelBuilder.Entity<PropertyType>(entity =>
        {
            entity.HasKey(e => e.PropertyTypeId).HasName("PK__Property__BDE14DD4B69510C0");

            entity.HasIndex(e => e.TypeName, "UQ_PropertyTypes_TypeName").IsUnique();

            entity.Property(e => e.PropertyTypeId).HasColumnName("PropertyTypeID");
            entity.Property(e => e.IsActive).HasDefaultValue(true, "DF_PropertyTypes_IsActive");
            entity.Property(e => e.TypeName).HasMaxLength(100);
        });

        modelBuilder.Entity<Province>(entity =>
        {
            entity.HasKey(e => e.ProvinceId).HasName("PK__Province__FD0A6FA322DA67CF");

            entity.HasIndex(e => e.ProvinceName, "UQ_Provinces_Name").IsUnique();

            entity.HasIndex(e => e.ProvinceCode, "UX_Provinces_Code_NotNull")
                .IsUnique()
                .HasFilter("([ProvinceCode] IS NOT NULL)");

            entity.Property(e => e.ProvinceId).HasColumnName("ProvinceID");
            entity.Property(e => e.IsActive).HasDefaultValue(true, "DF_Provinces_IsActive");
            entity.Property(e => e.ProvinceCode)
                .HasMaxLength(20)
                .IsUnicode(false);
            entity.Property(e => e.ProvinceName).HasMaxLength(150);
            entity.Property(e => e.ProvinceType)
                .HasMaxLength(20)
                .IsUnicode(false)
                .HasDefaultValue("PROVINCE", "DF_Provinces_Type");
        });

        modelBuilder.Entity<RefreshToken>(entity =>
        {
            entity.HasKey(e => e.RefreshTokenId).HasName("PK__RefreshT__F5845E597BFF2C0D");

            entity.HasIndex(e => e.ExpiresAt, "IX_RefreshTokens_ExpiresAt");

            entity.HasIndex(e => e.UserId, "IX_RefreshTokens_UserID");

            entity.HasIndex(e => e.TokenHash, "UQ_RefreshTokens_TokenHash").IsUnique();

            entity.Property(e => e.RefreshTokenId).HasColumnName("RefreshTokenID");
            entity.Property(e => e.CreatedAt).HasDefaultValueSql("(sysutcdatetime())", "DF_RefreshTokens_CreatedAt");
            entity.Property(e => e.TokenHash)
                .HasMaxLength(500)
                .IsUnicode(false);
            entity.Property(e => e.UserId).HasColumnName("UserID");

            entity.HasOne(d => d.User).WithMany(p => p.RefreshTokens)
                .HasForeignKey(d => d.UserId)
                .HasConstraintName("FK_RefreshTokens_Users");
        });

        modelBuilder.Entity<RentalContract>(entity =>
        {
            entity.HasKey(e => e.ContractId).HasName("PK__RentalCo__C90D3409075E04A5");

            entity.HasIndex(e => new { e.RoomId, e.Status }, "IX_RentalContracts_RoomID");

            entity.HasIndex(e => new { e.Status, e.StartDate, e.EndDate }, "IX_RentalContracts_Status_Dates");

            entity.HasIndex(e => e.ContractCode, "UQ_RentalContracts_ContractCode").IsUnique();

            entity.HasIndex(e => e.RentalRequestId, "UQ_RentalContracts_RentalRequestID").IsUnique();

            entity.HasIndex(e => e.RoomId, "UX_RentalContracts_OneActivePerRoom")
                .IsUnique()
                .HasFilter("([Status]='ACTIVE')");

            entity.Property(e => e.ContractId).HasColumnName("ContractID");
            entity.Property(e => e.ContractCode)
                .HasMaxLength(50)
                .IsUnicode(false);
            entity.Property(e => e.ContractFileUrl)
                .HasMaxLength(1000)
                .HasColumnName("ContractFileURL");
            entity.Property(e => e.CreatedAt).HasDefaultValueSql("(sysutcdatetime())", "DF_RentalContracts_CreatedAt");
            entity.Property(e => e.DepositAmount).HasColumnType("decimal(18, 2)");
            entity.Property(e => e.MonthlyRent).HasColumnType("decimal(18, 2)");
            entity.Property(e => e.RentalRequestId).HasColumnName("RentalRequestID");
            entity.Property(e => e.RoomId).HasColumnName("RoomID");
            entity.Property(e => e.Status)
                .HasMaxLength(20)
                .IsUnicode(false)
                .HasDefaultValue("PENDING", "DF_RentalContracts_Status");

            entity.HasOne(d => d.RentalRequest).WithMany(p => p.RentalContracts)
                .HasPrincipalKey(p => new { p.RentalRequestId, p.RoomId })
                .HasForeignKey(d => new { d.RentalRequestId, d.RoomId })
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_RentalContracts_RequestRoom");
        });

        modelBuilder.Entity<RentalRequest>(entity =>
        {
            entity.HasKey(e => e.RentalRequestId).HasName("PK__RentalRe__FD28476536980A98");

            entity.HasIndex(e => new { e.RoomId, e.Status }, "IX_RentalRequests_RoomID_Status");

            entity.HasIndex(e => new { e.Status, e.CreatedAt }, "IX_RentalRequests_Status_CreatedAt").IsDescending(false, true);

            entity.HasIndex(e => new { e.StudentId, e.CreatedAt }, "IX_RentalRequests_StudentID").IsDescending(false, true);

            entity.HasIndex(e => new { e.RentalRequestId, e.RoomId }, "UQ_RentalRequests_Request_Room").IsUnique();

            entity.HasIndex(e => new { e.StudentId, e.RoomId }, "UX_RentalRequests_Student_Room_Pending")
                .IsUnique()
                .HasFilter("([Status]='PENDING')");

            entity.Property(e => e.RentalRequestId).HasColumnName("RentalRequestID");
            entity.Property(e => e.CreatedAt).HasDefaultValueSql("(sysutcdatetime())", "DF_RentalRequests_CreatedAt");
            entity.Property(e => e.LandlordNote).HasMaxLength(1500);
            entity.Property(e => e.Note).HasMaxLength(1500);
            entity.Property(e => e.RoomId).HasColumnName("RoomID");
            entity.Property(e => e.Status)
                .HasMaxLength(20)
                .IsUnicode(false)
                .HasDefaultValue("PENDING", "DF_RentalRequests_Status");
            entity.Property(e => e.StudentId).HasColumnName("StudentID");
            entity.Property(e => e.UpdatedAt).HasDefaultValueSql("(sysutcdatetime())", "DF_RentalRequests_UpdatedAt");

            entity.HasOne(d => d.Room).WithMany(p => p.RentalRequests)
                .HasForeignKey(d => d.RoomId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_RentalRequests_Rooms");

            entity.HasOne(d => d.Student).WithMany(p => p.RentalRequests)
                .HasForeignKey(d => d.StudentId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_RentalRequests_Students");
        });

        modelBuilder.Entity<Report>(entity =>
        {
            entity.HasKey(e => e.ReportId).HasName("PK__Reports__D5BD48E549D1B9A4");

            entity.HasIndex(e => new { e.ReporterUserId, e.CreatedAt }, "IX_Reports_ReporterUserID").IsDescending(false, true);

            entity.HasIndex(e => new { e.RoomId, e.Status }, "IX_Reports_RoomID_Status");

            entity.HasIndex(e => new { e.Status, e.CreatedAt }, "IX_Reports_Status_CreatedAt").IsDescending(false, true);

            entity.Property(e => e.ReportId).HasColumnName("ReportID");
            entity.Property(e => e.AdminNote).HasMaxLength(2000);
            entity.Property(e => e.CreatedAt).HasDefaultValueSql("(sysutcdatetime())", "DF_Reports_CreatedAt");
            entity.Property(e => e.Description).HasMaxLength(2000);
            entity.Property(e => e.Reason).HasMaxLength(255);
            entity.Property(e => e.ReporterUserId).HasColumnName("ReporterUserID");
            entity.Property(e => e.ResolvedByUserId).HasColumnName("ResolvedByUserID");
            entity.Property(e => e.RoomId).HasColumnName("RoomID");
            entity.Property(e => e.Status)
                .HasMaxLength(20)
                .IsUnicode(false)
                .HasDefaultValue("OPEN", "DF_Reports_Status");

            entity.HasOne(d => d.ReporterUser).WithMany(p => p.ReportReporterUsers)
                .HasForeignKey(d => d.ReporterUserId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_Reports_Reporter");

            entity.HasOne(d => d.ResolvedByUser).WithMany(p => p.ReportResolvedByUsers)
                .HasForeignKey(d => d.ResolvedByUserId)
                .HasConstraintName("FK_Reports_ResolvedBy");

            entity.HasOne(d => d.Room).WithMany(p => p.Reports)
                .HasForeignKey(d => d.RoomId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_Reports_Rooms");
        });

        modelBuilder.Entity<Review>(entity =>
        {
            entity.HasKey(e => e.ReviewId).HasName("PK__Reviews__74BC79AE44A9944E");

            entity.HasIndex(e => e.Rating, "IX_Reviews_Rating");

            entity.HasIndex(e => new { e.Status, e.CreatedAt }, "IX_Reviews_Status_CreatedAt").IsDescending(false, true);

            entity.HasIndex(e => e.ContractId, "UQ_Reviews_ContractID").IsUnique();

            entity.Property(e => e.ReviewId).HasColumnName("ReviewID");
            entity.Property(e => e.Comment).HasMaxLength(2000);
            entity.Property(e => e.ContractId).HasColumnName("ContractID");
            entity.Property(e => e.CreatedAt).HasDefaultValueSql("(sysutcdatetime())", "DF_Reviews_CreatedAt");
            entity.Property(e => e.ModeratedByUserId).HasColumnName("ModeratedByUserID");
            entity.Property(e => e.Status)
                .HasMaxLength(20)
                .IsUnicode(false)
                .HasDefaultValue("VISIBLE", "DF_Reviews_Status");
            entity.Property(e => e.UpdatedAt).HasDefaultValueSql("(sysutcdatetime())", "DF_Reviews_UpdatedAt");

            entity.HasOne(d => d.Contract).WithOne(p => p.Review)
                .HasForeignKey<Review>(d => d.ContractId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_Reviews_Contracts");

            entity.HasOne(d => d.ModeratedByUser).WithMany(p => p.Reviews)
                .HasForeignKey(d => d.ModeratedByUserId)
                .HasConstraintName("FK_Reviews_ModeratedBy");
        });

        modelBuilder.Entity<Role>(entity =>
        {
            entity.HasKey(e => e.RoleId).HasName("PK__Roles__8AFACE3A33A00FED");

            entity.HasIndex(e => e.RoleCode, "UQ_Roles_RoleCode").IsUnique();

            entity.Property(e => e.RoleId).HasColumnName("RoleID");
            entity.Property(e => e.RoleCode)
                .HasMaxLength(30)
                .IsUnicode(false);
            entity.Property(e => e.RoleName).HasMaxLength(100);
        });

        modelBuilder.Entity<Room>(entity =>
        {
            entity.HasKey(e => e.RoomId).HasName("PK__Rooms__32863919224AC045");

            entity.HasIndex(e => e.AvailableFrom, "IX_Rooms_AvailableFrom");

            entity.HasIndex(e => e.CreatedAt, "IX_Rooms_CreatedAt").IsDescending();

            entity.HasIndex(e => e.PropertyId, "IX_Rooms_PropertyID");

            entity.HasIndex(e => new { e.ListingStatus, e.CreatedAt }, "IX_Rooms_ReviewStatus").IsDescending(false, true);

            entity.HasIndex(e => new { e.ListingStatus, e.AvailabilityStatus, e.RentPrice, e.AreaM2 }, "IX_Rooms_Search");

            entity.HasIndex(e => new { e.PropertyId, e.RoomNumber }, "UX_Rooms_Property_RoomNumber")
                .IsUnique()
                .HasFilter("([RoomNumber] IS NOT NULL)");

            entity.Property(e => e.RoomId).HasColumnName("RoomID");
            entity.Property(e => e.AreaM2).HasColumnType("decimal(8, 2)");
            entity.Property(e => e.AvailabilityStatus)
                .HasMaxLength(20)
                .IsUnicode(false)
                .HasDefaultValue("AVAILABLE", "DF_Rooms_AvailabilityStatus");
            entity.Property(e => e.CreatedAt).HasDefaultValueSql("(sysutcdatetime())", "DF_Rooms_CreatedAt");
            entity.Property(e => e.DepositAmount).HasColumnType("decimal(18, 2)");
            entity.Property(e => e.ElectricityPrice).HasColumnType("decimal(18, 2)");
            entity.Property(e => e.GenderPreference)
                .HasMaxLength(20)
                .IsUnicode(false)
                .HasDefaultValue("ALL", "DF_Rooms_GenderPreference");
            entity.Property(e => e.InternetPrice).HasColumnType("decimal(18, 2)");
            entity.Property(e => e.ListingStatus)
                .HasMaxLength(30)
                .IsUnicode(false)
                .HasDefaultValue("DRAFT", "DF_Rooms_ListingStatus");
            entity.Property(e => e.MaxOccupants).HasDefaultValue(1, "DF_Rooms_MaxOccupants");
            entity.Property(e => e.ParkingFee).HasColumnType("decimal(18, 2)");
            entity.Property(e => e.PropertyId).HasColumnName("PropertyID");
            entity.Property(e => e.RejectionReason).HasMaxLength(1000);
            entity.Property(e => e.RentPrice).HasColumnType("decimal(18, 2)");
            entity.Property(e => e.ReviewedByUserId).HasColumnName("ReviewedByUserID");
            entity.Property(e => e.RoomNumber).HasMaxLength(50);
            entity.Property(e => e.ServiceFee).HasColumnType("decimal(18, 2)");
            entity.Property(e => e.Title).HasMaxLength(255);
            entity.Property(e => e.UpdatedAt).HasDefaultValueSql("(sysutcdatetime())", "DF_Rooms_UpdatedAt");
            entity.Property(e => e.WaterPrice).HasColumnType("decimal(18, 2)");

            entity.HasOne(d => d.Property).WithMany(p => p.Rooms)
                .HasForeignKey(d => d.PropertyId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_Rooms_Properties");

            entity.HasOne(d => d.ReviewedByUser).WithMany(p => p.Rooms)
                .HasForeignKey(d => d.ReviewedByUserId)
                .HasConstraintName("FK_Rooms_ReviewedByUser");

            entity.HasMany(d => d.Amenities).WithMany(p => p.Rooms)
                .UsingEntity<Dictionary<string, object>>(
                    "RoomAmenity",
                    r => r.HasOne<Amenity>().WithMany()
                        .HasForeignKey("AmenityId")
                        .OnDelete(DeleteBehavior.ClientSetNull)
                        .HasConstraintName("FK_RoomAmenities_Amenities"),
                    l => l.HasOne<Room>().WithMany()
                        .HasForeignKey("RoomId")
                        .HasConstraintName("FK_RoomAmenities_Rooms"),
                    j =>
                    {
                        j.HasKey("RoomId", "AmenityId");
                        j.ToTable("RoomAmenities");
                        j.HasIndex(new[] { "AmenityId", "RoomId" }, "IX_RoomAmenities_AmenityID");
                        j.IndexerProperty<long>("RoomId").HasColumnName("RoomID");
                        j.IndexerProperty<int>("AmenityId").HasColumnName("AmenityID");
                    });
        });

        modelBuilder.Entity<RoomImage>(entity =>
        {
            entity.HasKey(e => e.ImageId).HasName("PK__RoomImag__7516F4EC52C37B0C");

            entity.HasIndex(e => new { e.RoomId, e.SortOrder }, "IX_RoomImages_RoomID");

            entity.HasIndex(e => new { e.RoomId, e.IsPrimary }, "UX_RoomImages_OnePrimaryPerRoom")
                .IsUnique()
                .HasFilter("([IsPrimary]=(1))");

            entity.HasIndex(e => e.PublicId, "UX_RoomImages_PublicId_NotNull")
                .IsUnique()
                .HasFilter("([PublicId] IS NOT NULL)");

            entity.Property(e => e.ImageId).HasColumnName("ImageID");
            entity.Property(e => e.CreatedAt).HasDefaultValueSql("(sysutcdatetime())", "DF_RoomImages_CreatedAt");
            entity.Property(e => e.ImageUrl)
                .HasMaxLength(1000)
                .HasColumnName("ImageURL");
            entity.Property(e => e.PublicId).HasMaxLength(500);
            entity.Property(e => e.RoomId).HasColumnName("RoomID");

            entity.HasOne(d => d.Room).WithMany(p => p.RoomImages)
                .HasForeignKey(d => d.RoomId)
                .HasConstraintName("FK_RoomImages_Rooms");
        });

        modelBuilder.Entity<RoomViewHistory>(entity =>
        {
            entity.HasKey(e => e.RoomViewHistoryId).HasName("PK__RoomView__7EA29973B33AAA43");

            entity.HasIndex(e => e.RoomId, "IX_RoomViewHistories_RoomID");

            entity.HasIndex(e => new { e.StudentId, e.LastViewedAt }, "IX_RoomViewHistories_Student_LastViewedAt").IsDescending(false, true);

            entity.HasIndex(e => new { e.StudentId, e.RoomId }, "UQ_RoomViewHistories_Student_Room").IsUnique();

            entity.Property(e => e.RoomViewHistoryId).HasColumnName("RoomViewHistoryID");
            entity.Property(e => e.FirstViewedAt).HasDefaultValueSql("(sysutcdatetime())", "DF_RoomViewHistories_FirstViewedAt");
            entity.Property(e => e.LastViewedAt).HasDefaultValueSql("(sysutcdatetime())", "DF_RoomViewHistories_LastViewedAt");
            entity.Property(e => e.RoomId).HasColumnName("RoomID");
            entity.Property(e => e.StudentId).HasColumnName("StudentID");
            entity.Property(e => e.ViewCount).HasDefaultValue(1, "DF_RoomViewHistories_ViewCount");

            entity.HasOne(d => d.Room).WithMany(p => p.RoomViewHistories)
                .HasForeignKey(d => d.RoomId)
                .HasConstraintName("FK_RoomViewHistories_Rooms");

            entity.HasOne(d => d.Student).WithMany(p => p.RoomViewHistories)
                .HasForeignKey(d => d.StudentId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_RoomViewHistories_Students");
        });

        modelBuilder.Entity<SavedRoom>(entity =>
        {
            entity.HasKey(e => new { e.StudentId, e.RoomId });

            entity.HasIndex(e => e.RoomId, "IX_SavedRooms_RoomID");

            entity.HasIndex(e => new { e.StudentId, e.SavedAt }, "IX_SavedRooms_Student_SavedAt").IsDescending(false, true);

            entity.Property(e => e.StudentId).HasColumnName("StudentID");
            entity.Property(e => e.RoomId).HasColumnName("RoomID");
            entity.Property(e => e.SavedAt).HasDefaultValueSql("(sysutcdatetime())", "DF_SavedRooms_SavedAt");

            entity.HasOne(d => d.Room).WithMany(p => p.SavedRooms)
                .HasForeignKey(d => d.RoomId)
                .HasConstraintName("FK_SavedRooms_Rooms");

            entity.HasOne(d => d.Student).WithMany(p => p.SavedRooms)
                .HasForeignKey(d => d.StudentId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_SavedRooms_Students");
        });

        modelBuilder.Entity<StudentProfile>(entity =>
        {
            entity.HasKey(e => e.StudentId).HasName("PK__StudentP__32C52A79D3512A9C");

            entity.HasIndex(e => e.UniversityId, "IX_StudentProfiles_UniversityID");

            entity.HasIndex(e => e.UserId, "UQ_StudentProfiles_UserID").IsUnique();

            entity.HasIndex(e => new { e.UniversityId, e.StudentCode }, "UX_StudentProfiles_University_StudentCode")
                .IsUnique()
                .HasFilter("([UniversityID] IS NOT NULL AND [StudentCode] IS NOT NULL)");

            entity.Property(e => e.StudentId).HasColumnName("StudentID");
            entity.Property(e => e.CreatedAt).HasDefaultValueSql("(sysutcdatetime())", "DF_StudentProfiles_CreatedAt");
            entity.Property(e => e.StudentCardUrl)
                .HasMaxLength(1000)
                .HasColumnName("StudentCardURL");
            entity.Property(e => e.StudentCode)
                .HasMaxLength(50)
                .IsUnicode(false);
            entity.Property(e => e.UniversityId).HasColumnName("UniversityID");
            entity.Property(e => e.UpdatedAt).HasDefaultValueSql("(sysutcdatetime())", "DF_StudentProfiles_UpdatedAt");
            entity.Property(e => e.UserId).HasColumnName("UserID");
            entity.Property(e => e.VerifiedByUserId).HasColumnName("VerifiedByUserID");

            entity.HasOne(d => d.University).WithMany(p => p.StudentProfiles)
                .HasForeignKey(d => d.UniversityId)
                .HasConstraintName("FK_StudentProfiles_Universities");

            entity.HasOne(d => d.User).WithOne(p => p.StudentProfileUser)
                .HasForeignKey<StudentProfile>(d => d.UserId)
                .HasConstraintName("FK_StudentProfiles_Users");

            entity.HasOne(d => d.VerifiedByUser).WithMany(p => p.StudentProfileVerifiedByUsers)
                .HasForeignKey(d => d.VerifiedByUserId)
                .HasConstraintName("FK_StudentProfiles_VerifiedBy");
        });

        modelBuilder.Entity<University>(entity =>
        {
            entity.HasKey(e => e.UniversityId).HasName("PK__Universi__9F19E19C286394A7");

            entity.HasIndex(e => new { e.ProvinceId, e.WardId }, "IX_Universities_Location");

            entity.HasIndex(e => e.UniversityName, "IX_Universities_Name");

            entity.Property(e => e.UniversityId).HasColumnName("UniversityID");
            entity.Property(e => e.AddressLine).HasMaxLength(500);
            entity.Property(e => e.IsActive).HasDefaultValue(true, "DF_Universities_IsActive");
            entity.Property(e => e.Latitude).HasColumnType("decimal(10, 7)");
            entity.Property(e => e.Longitude).HasColumnType("decimal(10, 7)");
            entity.Property(e => e.ProvinceId).HasColumnName("ProvinceID");
            entity.Property(e => e.UniversityName).HasMaxLength(255);
            entity.Property(e => e.WardId).HasColumnName("WardID");

            entity.HasOne(d => d.Province).WithMany(p => p.Universities)
                .HasForeignKey(d => d.ProvinceId)
                .HasConstraintName("FK_Universities_Provinces");

            entity.HasOne(d => d.Ward).WithMany(p => p.Universities)
                .HasPrincipalKey(p => new { p.ProvinceId, p.WardId })
                .HasForeignKey(d => new { d.ProvinceId, d.WardId })
                .HasConstraintName("FK_Universities_ProvinceWard");
        });

        modelBuilder.Entity<User>(entity =>
        {
            entity.HasKey(e => e.UserId).HasName("PK__Users__1788CCAC93D85EF0");

            entity.HasIndex(e => e.CreatedAt, "IX_Users_CreatedAt").IsDescending();

            entity.HasIndex(e => e.RoleId, "IX_Users_RoleID");

            entity.HasIndex(e => e.Status, "IX_Users_Status");

            entity.HasIndex(e => e.Email, "UQ_Users_Email").IsUnique();

            entity.HasIndex(e => e.Phone, "UX_Users_Phone_NotNull")
                .IsUnique()
                .HasFilter("([Phone] IS NOT NULL)");

            entity.Property(e => e.UserId).HasColumnName("UserID");
            entity.Property(e => e.AvatarUrl)
                .HasMaxLength(1000)
                .HasColumnName("AvatarURL");
            entity.Property(e => e.CreatedAt).HasDefaultValueSql("(sysutcdatetime())", "DF_Users_CreatedAt");
            entity.Property(e => e.Email)
                .HasMaxLength(255)
                .IsUnicode(false);
            entity.Property(e => e.FullName).HasMaxLength(150);
            entity.Property(e => e.LockedByUserId).HasColumnName("LockedByUserID");
            entity.Property(e => e.LockedReason).HasMaxLength(500);
            entity.Property(e => e.PasswordHash)
                .HasMaxLength(500)
                .IsUnicode(false);
            entity.Property(e => e.Phone)
                .HasMaxLength(20)
                .IsUnicode(false);
            entity.Property(e => e.RoleId).HasColumnName("RoleID");
            entity.Property(e => e.Status)
                .HasMaxLength(20)
                .IsUnicode(false)
                .HasDefaultValue("ACTIVE", "DF_Users_Status");
            entity.Property(e => e.UpdatedAt).HasDefaultValueSql("(sysutcdatetime())", "DF_Users_UpdatedAt");

            entity.HasOne(d => d.LockedByUser).WithMany(p => p.InverseLockedByUser)
                .HasForeignKey(d => d.LockedByUserId)
                .HasConstraintName("FK_Users_LockedBy");

            entity.HasOne(d => d.Role).WithMany(p => p.Users)
                .HasForeignKey(d => d.RoleId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_Users_Roles");
        });

        modelBuilder.Entity<UserDeviceToken>(entity =>
        {
            entity.HasKey(e => e.DeviceTokenId).HasName("PK__UserDevi__2373DC9727AA53F1");

            entity.HasIndex(e => new { e.UserId, e.IsActive }, "IX_UserDeviceTokens_UserID");

            entity.HasIndex(e => e.DeviceToken, "UQ_UserDeviceTokens_DeviceToken").IsUnique();

            entity.Property(e => e.DeviceTokenId).HasColumnName("DeviceTokenID");
            entity.Property(e => e.CreatedAt).HasDefaultValueSql("(sysutcdatetime())", "DF_UserDeviceTokens_CreatedAt");
            entity.Property(e => e.DeviceToken)
                .HasMaxLength(512)
                .IsUnicode(false);
            entity.Property(e => e.IsActive).HasDefaultValue(true, "DF_UserDeviceTokens_IsActive");
            entity.Property(e => e.LastSeenAt).HasDefaultValueSql("(sysutcdatetime())", "DF_UserDeviceTokens_LastSeenAt");
            entity.Property(e => e.Platform)
                .HasMaxLength(20)
                .IsUnicode(false)
                .HasDefaultValue("ANDROID", "DF_UserDeviceTokens_Platform");
            entity.Property(e => e.UserId).HasColumnName("UserID");

            entity.HasOne(d => d.User).WithMany(p => p.UserDeviceTokens)
                .HasForeignKey(d => d.UserId)
                .HasConstraintName("FK_UserDeviceTokens_Users");
        });

        modelBuilder.Entity<ViewingAppointment>(entity =>
        {
            entity.HasKey(e => e.AppointmentId).HasName("PK__ViewingA__8ECDFCA2BB860C2A");

            entity.HasIndex(e => new { e.RoomId, e.Status }, "IX_ViewingAppointments_RoomID_Status");

            entity.HasIndex(e => e.ScheduledAt, "IX_ViewingAppointments_ScheduledAt");

            entity.HasIndex(e => new { e.StudentId, e.CreatedAt }, "IX_ViewingAppointments_StudentID").IsDescending(false, true);

            entity.Property(e => e.AppointmentId).HasColumnName("AppointmentID");
            entity.Property(e => e.CreatedAt).HasDefaultValueSql("(sysutcdatetime())", "DF_ViewingAppointments_CreatedAt");
            entity.Property(e => e.LandlordNote).HasMaxLength(1000);
            entity.Property(e => e.Note).HasMaxLength(1000);
            entity.Property(e => e.RoomId).HasColumnName("RoomID");
            entity.Property(e => e.Status)
                .HasMaxLength(20)
                .IsUnicode(false)
                .HasDefaultValue("PENDING", "DF_ViewingAppointments_Status");
            entity.Property(e => e.StudentId).HasColumnName("StudentID");
            entity.Property(e => e.UpdatedAt).HasDefaultValueSql("(sysutcdatetime())", "DF_ViewingAppointments_UpdatedAt");

            entity.HasOne(d => d.Room).WithMany(p => p.ViewingAppointments)
                .HasForeignKey(d => d.RoomId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_ViewingAppointments_Rooms");

            entity.HasOne(d => d.Student).WithMany(p => p.ViewingAppointments)
                .HasForeignKey(d => d.StudentId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_ViewingAppointments_Students");
        });

        modelBuilder.Entity<VwRentalContractDetail>(entity =>
        {
            entity
                .HasNoKey()
                .ToView("vw_RentalContractDetails");

            entity.Property(e => e.ContractCode)
                .HasMaxLength(50)
                .IsUnicode(false);
            entity.Property(e => e.ContractFileUrl)
                .HasMaxLength(1000)
                .HasColumnName("ContractFileURL");
            entity.Property(e => e.ContractId).HasColumnName("ContractID");
            entity.Property(e => e.DepositAmount).HasColumnType("decimal(18, 2)");
            entity.Property(e => e.LandlordId).HasColumnName("LandlordID");
            entity.Property(e => e.MonthlyRent).HasColumnType("decimal(18, 2)");
            entity.Property(e => e.RentalRequestId).HasColumnName("RentalRequestID");
            entity.Property(e => e.RoomId).HasColumnName("RoomID");
            entity.Property(e => e.Status)
                .HasMaxLength(20)
                .IsUnicode(false);
            entity.Property(e => e.StudentId).HasColumnName("StudentID");
        });

        modelBuilder.Entity<Ward>(entity =>
        {
            entity.HasKey(e => e.WardId).HasName("PK__Wards__C6BD9BEA0AD795B9");

            entity.HasIndex(e => new { e.ProvinceId, e.WardName }, "IX_Wards_ProvinceID");

            entity.HasIndex(e => new { e.ProvinceId, e.WardName }, "UQ_Wards_Province_Name").IsUnique();

            entity.HasIndex(e => new { e.ProvinceId, e.WardId }, "UQ_Wards_Province_WardID").IsUnique();

            entity.HasIndex(e => e.WardCode, "UX_Wards_Code_NotNull")
                .IsUnique()
                .HasFilter("([WardCode] IS NOT NULL)");

            entity.Property(e => e.WardId).HasColumnName("WardID");
            entity.Property(e => e.IsActive).HasDefaultValue(true, "DF_Wards_IsActive");
            entity.Property(e => e.ProvinceId).HasColumnName("ProvinceID");
            entity.Property(e => e.WardCode)
                .HasMaxLength(20)
                .IsUnicode(false);
            entity.Property(e => e.WardName).HasMaxLength(150);
            entity.Property(e => e.WardType)
                .HasMaxLength(20)
                .IsUnicode(false)
                .HasDefaultValue("WARD", "DF_Wards_Type");

            entity.HasOne(d => d.Province).WithMany(p => p.Wards)
                .HasForeignKey(d => d.ProvinceId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_Wards_Provinces");
        });

        OnModelCreatingPartial(modelBuilder);
    }

    partial void OnModelCreatingPartial(ModelBuilder modelBuilder);
}
