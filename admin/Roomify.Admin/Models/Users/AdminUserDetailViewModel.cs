namespace Roomify.Admin.Models.Users;

public class AdminUserDetailViewModel
{
    public AdminUserDto? User { get; set; }

    public string? ErrorMessage { get; set; }

    public string? ReturnUrl { get; set; }


    public bool HasError =>
        !string.IsNullOrWhiteSpace(ErrorMessage);


    public bool IsStudent =>
        string.Equals(
            User?.RoleCode,
            "STUDENT",
            StringComparison.OrdinalIgnoreCase);


    public bool IsLandlord =>
        string.Equals(
            User?.RoleCode,
            "LANDLORD",
            StringComparison.OrdinalIgnoreCase);


    public bool IsAdmin =>
        string.Equals(
            User?.RoleCode,
            "ADMIN",
            StringComparison.OrdinalIgnoreCase);


    public bool IsLocked =>
        string.Equals(
            User?.Status,
            "LOCKED",
            StringComparison.OrdinalIgnoreCase);
}