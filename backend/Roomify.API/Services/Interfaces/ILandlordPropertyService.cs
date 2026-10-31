using Roomify.API.DTOs.Landlord;

namespace Roomify.API.Services.Interfaces;

public interface ILandlordPropertyService
{
    Task<IReadOnlyList<PropertyResponse>> GetPropertiesAsync(
        long userId,
        CancellationToken cancellationToken = default);

    Task<PropertyResponse> GetPropertyAsync(
        long userId,
        long propertyId,
        CancellationToken cancellationToken = default);

    Task<PropertyResponse> CreatePropertyAsync(
        long userId,
        CreatePropertyRequest request,
        CancellationToken cancellationToken = default);

    Task<PropertyResponse> UpdatePropertyAsync(
        long userId,
        long propertyId,
        UpdatePropertyRequest request,
        CancellationToken cancellationToken = default);

    Task<PropertyResponse> UpdatePropertyStatusAsync(
        long userId,
        long propertyId,
        UpdatePropertyStatusRequest request,
        CancellationToken cancellationToken = default);

    Task<IReadOnlyList<RoomResponse>> GetRoomsAsync(
        long userId,
        long propertyId,
        CancellationToken cancellationToken = default);

    Task<RoomResponse> GetRoomAsync(
        long userId,
        long roomId,
        CancellationToken cancellationToken = default);

    Task<RoomResponse> CreateRoomAsync(
        long userId,
        long propertyId,
        CreateRoomRequest request,
        CancellationToken cancellationToken = default);

    Task<RoomResponse> UpdateRoomAsync(
        long userId,
        long roomId,
        UpdateRoomRequest request,
        CancellationToken cancellationToken = default);

    Task<RoomResponse> UpdateAmenitiesAsync(
        long userId,
        long roomId,
        UpdateRoomAmenitiesRequest request,
        CancellationToken cancellationToken = default);

    Task<RoomResponse> UpdateAvailabilityAsync(
        long userId,
        long roomId,
        UpdateRoomAvailabilityRequest request,
        CancellationToken cancellationToken = default);

    Task<RoomResponse> SubmitForReviewAsync(
        long userId,
        long roomId,
        CancellationToken cancellationToken = default);

    Task<RoomResponse> HideRoomAsync(
        long userId,
        long roomId,
        CancellationToken cancellationToken = default);
}