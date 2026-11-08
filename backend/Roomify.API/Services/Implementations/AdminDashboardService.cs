using Microsoft.EntityFrameworkCore;
using Roomify.API.Data;
using Roomify.API.DTOs.Admin.Dashboard;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Services.Implementations;

public class AdminDashboardService : IAdminDashboardService
{
    private readonly RoomifyDbContext _db;

    public AdminDashboardService(RoomifyDbContext db)
    {
        _db = db;
    }

    public async Task<AdminDashboardResponse> GetDashboardAsync()
    {
        // ============================================================
        // USERS
        // ============================================================

        var totalUsers = await _db.Users
            .AsNoTracking()
            .CountAsync();

        var activeUsers = await _db.Users
            .AsNoTracking()
            .CountAsync(x =>
                x.Status == "ACTIVE");

        var lockedUsers = await _db.Users
            .AsNoTracking()
            .CountAsync(x =>
                x.Status == "LOCKED");


        // ============================================================
        // STUDENTS
        // ============================================================

        var totalStudents = await _db.StudentProfiles
            .AsNoTracking()
            .CountAsync();

        var verifiedStudents = await _db.StudentProfiles
            .AsNoTracking()
            .CountAsync(x =>
                x.IsVerified);


        // ============================================================
        // LANDLORDS
        // ============================================================

        var totalLandlords = await _db.LandlordProfiles
            .AsNoTracking()
            .CountAsync();

        var verifiedLandlords = await _db.LandlordProfiles
            .AsNoTracking()
            .CountAsync(x =>
                x.IsVerified);


        // ============================================================
        // PROPERTIES
        // ============================================================

        var totalProperties = await _db.Properties
            .AsNoTracking()
            .CountAsync();


        // ============================================================
        // ROOMS
        // ============================================================

        var totalRooms = await _db.Rooms
            .AsNoTracking()
            .CountAsync();

        var pendingRooms = await _db.Rooms
            .AsNoTracking()
            .CountAsync(x =>
                x.ListingStatus == "PENDING");

        var publishedRooms = await _db.Rooms
            .AsNoTracking()
            .CountAsync(x =>
                x.ListingStatus == "PUBLISHED");

        var rejectedRooms = await _db.Rooms
            .AsNoTracking()
            .CountAsync(x =>
                x.ListingStatus == "REJECTED");


        // ============================================================
        // REPORTS
        // ============================================================

        var openReports = await _db.Reports
            .AsNoTracking()
            .CountAsync(x =>
                x.Status == "OPEN");

        var resolvedReports = await _db.Reports
            .AsNoTracking()
            .CountAsync(x =>
                x.Status == "RESOLVED");

        var rejectedReports = await _db.Reports
            .AsNoTracking()
            .CountAsync(x =>
                x.Status == "REJECTED");


        // ============================================================
        // RENTAL CONTRACTS
        // ============================================================

        var totalContracts = await _db.RentalContracts
            .AsNoTracking()
            .CountAsync();

        var activeContracts = await _db.RentalContracts
            .AsNoTracking()
            .CountAsync(x =>
                x.Status == "ACTIVE");

        var completedContracts = await _db.RentalContracts
            .AsNoTracking()
            .CountAsync(x =>
                x.Status == "COMPLETED");


        // ============================================================
        // PAYMENTS
        // ============================================================

        var totalPayments = await _db.Payments
            .AsNoTracking()
            .CountAsync();

        var pendingPayments = await _db.Payments
            .AsNoTracking()
            .CountAsync(x =>
                x.Status == "PENDING");

        var paidPayments = await _db.Payments
            .AsNoTracking()
            .CountAsync(x =>
                x.Status == "PAID");

        var totalPaidAmount = await _db.Payments
            .AsNoTracking()
            .Where(x =>
                x.Status == "PAID")
            .SumAsync(x =>
                (decimal?)x.Amount)
            ?? 0m;


        // ============================================================
        // RESPONSE
        // ============================================================

        return new AdminDashboardResponse
        {
            // USERS
            TotalUsers = totalUsers,
            ActiveUsers = activeUsers,
            LockedUsers = lockedUsers,

            TotalStudents = totalStudents,
            VerifiedStudents = verifiedStudents,

            TotalLandlords = totalLandlords,
            VerifiedLandlords = verifiedLandlords,


            // PROPERTIES / ROOMS
            TotalProperties = totalProperties,

            TotalRooms = totalRooms,
            PendingRooms = pendingRooms,
            PublishedRooms = publishedRooms,
            RejectedRooms = rejectedRooms,


            // REPORTS
            OpenReports = openReports,
            ResolvedReports = resolvedReports,
            RejectedReports = rejectedReports,


            // CONTRACTS
            TotalContracts = totalContracts,
            ActiveContracts = activeContracts,
            CompletedContracts = completedContracts,


            // PAYMENTS
            TotalPayments = totalPayments,
            PendingPayments = pendingPayments,
            PaidPayments = paidPayments,
            TotalPaidAmount = totalPaidAmount
        };
    }
}