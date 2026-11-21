using Roomify.Admin.Models.Common;
using Roomify.Admin.Models.Rooms;

namespace Roomify.Admin.Services.Rooms;

public interface IAdminRoomApiService
{
    Task<ApiResult<AdminRoomListDto>> GetRoomsAsync(
        string? listingStatus = null,
        string? search = null,
        int page = 1,
        int pageSize = 10,
        CancellationToken cancellationToken = default);


    Task<ApiResult<AdminRoomDto>> GetRoomByIdAsync(
        long roomId,
        CancellationToken cancellationToken = default);


    Task<ApiResult<AdminRoomDto>> ApproveRoomAsync(
        long roomId,
        CancellationToken cancellationToken = default);


    Task<ApiResult<AdminRoomDto>> RejectRoomAsync(
        long roomId,
        string reason,
        CancellationToken cancellationToken = default);
}