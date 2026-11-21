namespace Roomify.Admin.Models.Rooms;

public class AdminRoomDetailViewModel
{
    public AdminRoomDto? Room { get; set; }

    public string? ErrorMessage { get; set; }

    public string? ReturnUrl { get; set; }

    public bool HasError =>
        !string.IsNullOrWhiteSpace(ErrorMessage);

    public bool IsPending =>
        string.Equals(
            Room?.ListingStatus,
            "PENDING",
            StringComparison.OrdinalIgnoreCase);

    public bool IsPublished =>
        string.Equals(
            Room?.ListingStatus,
            "PUBLISHED",
            StringComparison.OrdinalIgnoreCase);

    public bool IsRejected =>
        string.Equals(
            Room?.ListingStatus,
            "REJECTED",
            StringComparison.OrdinalIgnoreCase);
}