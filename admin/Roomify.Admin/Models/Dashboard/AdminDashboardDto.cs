namespace Roomify.Admin.Models.Dashboard;

public class AdminDashboardDto
{
    public int TotalUsers { get; set; }

    public int ActiveUsers { get; set; }

    public int LockedUsers { get; set; }

    public int TotalStudents { get; set; }

    public int VerifiedStudents { get; set; }

    public int TotalLandlords { get; set; }

    public int VerifiedLandlords { get; set; }


    public int TotalProperties { get; set; }

    public int TotalRooms { get; set; }

    public int PendingRooms { get; set; }

    public int PublishedRooms { get; set; }

    public int RejectedRooms { get; set; }


    public int OpenReports { get; set; }

    public int ResolvedReports { get; set; }

    public int RejectedReports { get; set; }


    public int TotalContracts { get; set; }

    public int ActiveContracts { get; set; }

    public int CompletedContracts { get; set; }


    public int TotalPayments { get; set; }

    public int PendingPayments { get; set; }

    public int PaidPayments { get; set; }

    public decimal TotalPaidAmount { get; set; }
}