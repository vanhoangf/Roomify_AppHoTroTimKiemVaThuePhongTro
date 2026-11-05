
using Roomify.API.DTOs.Student;

namespace Roomify.API.Services.Interfaces;

public interface ISavedRoomService
{
    Task<IReadOnlyList<SavedRoomResponse>>
        GetSavedRoomsAsync(
            long userId,
            CancellationToken cancellationToken = default);

    Task<SavedRoomResponse> SaveRoomAsync(
        long userId,
        long roomId,
        CancellationToken cancellationToken = default);

    Task DeleteSavedRoomAsync(
        long userId,
        long roomId,
        CancellationToken cancellationToken = default);
}
