
using Microsoft.EntityFrameworkCore;
using Roomify.API.Entities;

namespace Roomify.API.Data;

public partial class RoomifyDbContext
{
    partial void OnModelCreatingPartial(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<ViewingAppointment>(entity =>
        {
            entity.Property(x => x.ProposedAt)
                .HasColumnName("ProposedAt");

            entity.Property(x => x.RowVer)
                .HasColumnName("RowVer")
                .IsRowVersion();
        });

        modelBuilder.Entity<ViewingAppointmentHistory>(entity =>
        {
            entity.ToTable("ViewingAppointmentHistories");

            entity.HasKey(x => x.AppointmentHistoryId);

            entity.Property(x => x.AppointmentHistoryId)
                .HasColumnName("AppointmentHistoryID");

            entity.Property(x => x.AppointmentId)
                .HasColumnName("AppointmentID");

            entity.Property(x => x.ActorUserId)
                .HasColumnName("ActorUserID");

            entity.Property(x => x.Action)
                .HasMaxLength(30)
                .IsUnicode(false);

            entity.Property(x => x.OldStatus)
                .HasMaxLength(20)
                .IsUnicode(false);

            entity.Property(x => x.NewStatus)
                .HasMaxLength(20)
                .IsUnicode(false);

            entity.Property(x => x.Note)
                .HasMaxLength(1000);

            entity.Property(x => x.CreatedAt)
                .HasDefaultValueSql("(sysutcdatetime())");

            entity.HasOne<ViewingAppointment>()
                .WithMany()
                .HasForeignKey(x => x.AppointmentId)
                .OnDelete(DeleteBehavior.Restrict);

            entity.HasOne<User>()
                .WithMany()
                .HasForeignKey(x => x.ActorUserId)
                .OnDelete(DeleteBehavior.Restrict);
        });
    }
}
