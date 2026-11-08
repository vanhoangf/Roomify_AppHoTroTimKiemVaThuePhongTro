using Roomify.API.DTOs.Admin.Rooms;

namespace Roomify.API.Services.Interfaces;

public interface IAdminRoomService
{
    Task<AdminRoomListResponse> GetRoomsAsync(
        string? listingStatus,
        string? search,
        int page,
        int pageSize);

    Task<AdminRoomResponse> GetRoomByIdAsync(
        long roomId);

    Task<AdminRoomResponse> ApproveAsync(
        long adminUserId,
        long roomId);

    Task<AdminRoomResponse> RejectAsync(
        long adminUserId,
        long roomId,
        RejectRoomRequest request);
}