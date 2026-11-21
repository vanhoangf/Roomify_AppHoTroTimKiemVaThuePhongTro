namespace Roomify.Admin.Models.Dashboard;

public class DashboardViewModel
{
    public AdminDashboardDto? Data { get; set; }

    public string? ErrorMessage { get; set; }


    public bool HasError =>
        !string.IsNullOrWhiteSpace(ErrorMessage);


    public bool IsEmpty =>
        Data is not null &&
        Data.TotalUsers == 0 &&
        Data.TotalRooms == 0 &&
        Data.TotalContracts == 0 &&
        Data.TotalPayments == 0;


    public int OtherUsers =>
        Data is null
            ? 0
            : Math.Max(
                0,
                Data.TotalUsers
                - Data.TotalStudents
                - Data.TotalLandlords);


    public int OtherRoomStatuses =>
        Data is null
            ? 0
            : Math.Max(
                0,
                Data.TotalRooms
                - Data.PendingRooms
                - Data.PublishedRooms
                - Data.RejectedRooms);


    public int OtherContracts =>
        Data is null
            ? 0
            : Math.Max(
                0,
                Data.TotalContracts
                - Data.ActiveContracts
                - Data.CompletedContracts);


    public int OtherPayments =>
        Data is null
            ? 0
            : Math.Max(
                0,
                Data.TotalPayments
                - Data.PendingPayments
                - Data.PaidPayments);


    public double StudentPercentage =>
        Percentage(
            Data?.TotalStudents ?? 0,
            Data?.TotalUsers ?? 0);


    public double LandlordPercentage =>
        Percentage(
            Data?.TotalLandlords ?? 0,
            Data?.TotalUsers ?? 0);


    public double VerifiedStudentPercentage =>
        Percentage(
            Data?.VerifiedStudents ?? 0,
            Data?.TotalStudents ?? 0);


    public double VerifiedLandlordPercentage =>
        Percentage(
            Data?.VerifiedLandlords ?? 0,
            Data?.TotalLandlords ?? 0);


    public double PublishedRoomPercentage =>
        Percentage(
            Data?.PublishedRooms ?? 0,
            Data?.TotalRooms ?? 0);


    public double PendingRoomPercentage =>
        Percentage(
            Data?.PendingRooms ?? 0,
            Data?.TotalRooms ?? 0);


    public double RejectedRoomPercentage =>
        Percentage(
            Data?.RejectedRooms ?? 0,
            Data?.TotalRooms ?? 0);


    private static double Percentage(
        int value,
        int total)
    {
        if (total <= 0)
        {
            return 0;
        }

        return Math.Round(
            value * 100d / total,
            1);
    }
}