using Roomify.API.DTOs.Public;

namespace Roomify.API.Services.Interfaces;

public interface IRoomDiscoveryService
{
    Task<PagedResponse<RoomCardResponse>>
        SearchRoomsAsync(
            SearchRoomsRequest request,
            CancellationToken cancellationToken = default);

    Task<RoomDetailResponse>
        GetRoomDetailAsync(
            long roomId,
            CancellationToken cancellationToken = default);
}