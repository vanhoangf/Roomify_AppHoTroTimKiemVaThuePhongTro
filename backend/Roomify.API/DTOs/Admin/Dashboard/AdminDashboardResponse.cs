namespace Roomify.API.DTOs.Admin.Dashboard;

public class AdminDashboardResponse
{
    // USERS
    public int TotalUsers { get; set; }

    public int ActiveUsers { get; set; }

    public int LockedUsers { get; set; }

    public int TotalStudents { get; set; }

    public int VerifiedStudents { get; set; }

    public int TotalLandlords { get; set; }

    public int VerifiedLandlords { get; set; }


    // PROPERTIES / ROOMS
    public int TotalProperties { get; set; }

    public int TotalRooms { get; set; }

    public int PendingRooms { get; set; }

    public int PublishedRooms { get; set; }

    public int RejectedRooms { get; set; }


    // REPORTS
    public int OpenReports { get; set; }

    public int ResolvedReports { get; set; }

    public int RejectedReports { get; set; }


    // RENTALS
    public int TotalContracts { get; set; }

    public int ActiveContracts { get; set; }

    public int CompletedContracts { get; set; }


    // PAYMENTS
    public int TotalPayments { get; set; }

    public int PendingPayments { get; set; }

    public int PaidPayments { get; set; }

    public decimal TotalPaidAmount { get; set; }
}