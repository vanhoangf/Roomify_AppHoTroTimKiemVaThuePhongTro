
using Roomify.API.DTOs.Public;
using Roomify.API.DTOs.Student;

namespace Roomify.API.Services.Interfaces;

public interface IViewingAppointmentService
{
    Task<ViewingAppointmentResponse> CreateAsync(
        long userId,
        long roomId,
        CreateViewingAppointmentRequest request,
        CancellationToken cancellationToken = default);

    Task<PagedResponse<ViewingAppointmentResponse>> GetListAsync(
        long userId,
        bool asLandlord,
        int page,
        int pageSize,
        CancellationToken cancellationToken = default);

    Task<ViewingAppointmentResponse> GetDetailAsync(
        long userId,
        long appointmentId,
        bool asLandlord,
        CancellationToken cancellationToken = default);

    Task<IReadOnlyList<ViewingAppointmentHistoryResponse>>
        GetHistoryAsync(
            long userId,
            long appointmentId,
            bool asLandlord,
            CancellationToken cancellationToken = default);

    Task<ViewingAppointmentResponse> UpdateAsync(
        long userId,
        long appointmentId,
        bool asLandlord,
        string action,
        DateTimeOffset? proposedAt,
        string? note,
        CancellationToken cancellationToken = default);
}
