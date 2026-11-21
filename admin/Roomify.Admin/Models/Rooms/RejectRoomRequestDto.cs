using System.ComponentModel.DataAnnotations;

namespace Roomify.Admin.Models.Rooms;

public class RejectRoomRequestDto
{
    [Required(
        ErrorMessage = "Vui lòng nhập lý do từ chối tin phòng.")]
    [MaxLength(
        1000,
        ErrorMessage = "Lý do từ chối không được vượt quá 1000 ký tự.")]
    public string Reason { get; set; } = string.Empty;
}