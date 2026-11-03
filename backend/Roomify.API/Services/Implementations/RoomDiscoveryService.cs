
using Microsoft.EntityFrameworkCore;

using Roomify.API.Data;
using Roomify.API.DTOs.Public;
using Roomify.API.Entities;
using Roomify.API.Exceptions;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Services.Implementations;

public class RoomDiscoveryService : IRoomDiscoveryService
{
    private readonly RoomifyDbContext _dbContext;

    public RoomDiscoveryService(RoomifyDbContext dbContext)
    {
        _dbContext = dbContext;
    }

    // ==================================================
    // SEARCH ROOMS
    // ==================================================

    public async Task<PagedResponse<RoomCardResponse>>
        SearchRoomsAsync(
            SearchRoomsRequest request,
            CancellationToken cancellationToken = default)
    {
        ValidateSearchRequest(request);

        var keyword = request.Keyword?.Trim();

        var sortBy = (request.SortBy ?? "newest")
            .Trim()
            .ToLowerInvariant();

        var availabilityStatus = request.AvailabilityStatus?
            .Trim()
            .ToUpperInvariant();

        var genderPreference = request.GenderPreference?
            .Trim()
            .ToUpperInvariant();

        var amenityIds = (request.AmenityIds ?? [])
            .Distinct()
            .ToArray();

        // ----------------------------------------------
        // VALIDATE LOCATION AND CATALOG
        // ----------------------------------------------

        if (request.ProvinceId.HasValue)
        {
            var exists = await _dbContext.Provinces
                .AsNoTracking()
                .AnyAsync(
                    x => x.ProvinceId == request.ProvinceId.Value
                        && x.IsActive,
                    cancellationToken);

            if (!exists)
            {
                throw new NotFoundException(
                    "PROVINCE_NOT_FOUND",
                    "Tỉnh/thành phố không tồn tại.");
            }
        }

        if (request.WardId.HasValue)
        {
            var exists = await _dbContext.Wards
                .AsNoTracking()
                .AnyAsync(
                    x => x.WardId == request.WardId.Value
                        && x.ProvinceId == request.ProvinceId!.Value
                        && x.IsActive,
                    cancellationToken);

            if (!exists)
            {
                throw new NotFoundException(
                    "WARD_NOT_FOUND",
                    "Xã/phường không tồn tại trong tỉnh/thành phố.");
            }
        }

        if (request.PropertyTypeId.HasValue)
        {
            var exists = await _dbContext.PropertyTypes
                .AsNoTracking()
                .AnyAsync(
                    x => x.PropertyTypeId == request.PropertyTypeId.Value
                        && x.IsActive,
                    cancellationToken);

            if (!exists)
            {
                throw new NotFoundException(
                    "PROPERTY_TYPE_NOT_FOUND",
                    "Loại khu trọ không tồn tại hoặc không hoạt động.");
            }
        }

        if (amenityIds.Length > 0)
        {
            var validCount = await _dbContext.Amenities
                .AsNoTracking()
                .CountAsync(
                    x => amenityIds.Contains(x.AmenityId)
                        && x.IsActive,
                    cancellationToken);

            if (validCount != amenityIds.Length)
            {
                throw new BadRequestException(
                    "INVALID_AMENITY",
                    "Một hoặc nhiều tiện ích không hợp lệ.");
            }
        }

        // ----------------------------------------------
        // UNIVERSITY LOCATION
        // ----------------------------------------------

        double universityLatitude = 0;
        double universityLongitude = 0;
        double longitudeFactor = 0;

        var filterByUniversity = request.UniversityId.HasValue;
        var radiusKm = request.RadiusKm ?? 5.0;

        if (filterByUniversity)
        {
            var university = await _dbContext.Universities
                .AsNoTracking()
                .Where(
                    x => x.UniversityId == request.UniversityId!.Value
                        && x.IsActive)
                .Select(x => new
                {
                    x.Latitude,
                    x.Longitude
                })
                .SingleOrDefaultAsync(cancellationToken);

            if (university is null)
            {
                throw new NotFoundException(
                    "UNIVERSITY_NOT_FOUND",
                    "Không tìm thấy trường đại học.");
            }

            if (!university.Latitude.HasValue ||
                !university.Longitude.HasValue)
            {
                throw new BadRequestException(
                    "UNIVERSITY_LOCATION_UNAVAILABLE",
                    "Trường đại học chưa có tọa độ.");
            }

            universityLatitude = (double)university.Latitude.Value;
            universityLongitude = (double)university.Longitude.Value;

            longitudeFactor = 111.32 * Math.Max(
                0.01,
                Math.Abs(
                    Math.Cos(universityLatitude * Math.PI / 180.0)));
        }

        // ----------------------------------------------
        // BASE QUERY
        // ----------------------------------------------

        IQueryable<Room> query = _dbContext.Rooms
            .AsNoTracking()
            .Where(x =>
                x.ListingStatus == "PUBLISHED" &&
                x.Property.Status == "ACTIVE" &&
                x.Property.PropertyType.IsActive);

        // ----------------------------------------------
        // KEYWORD
        // ----------------------------------------------

        if (!string.IsNullOrWhiteSpace(keyword))
        {
            query = query.Where(x =>
                x.Title.Contains(keyword) ||
                (x.Description != null &&
                    x.Description.Contains(keyword)) ||
                x.Property.PropertyName.Contains(keyword) ||
                x.Property.AddressLine.Contains(keyword));
        }

        // ----------------------------------------------
        // ADDRESS FILTER
        // ----------------------------------------------

        if (request.ProvinceId.HasValue)
        {
            query = query.Where(x =>
                x.Property.ProvinceId == request.ProvinceId.Value);
        }

        if (request.WardId.HasValue)
        {
            query = query.Where(x =>
                x.Property.WardId == request.WardId.Value);
        }

        if (request.PropertyTypeId.HasValue)
        {
            query = query.Where(x =>
                x.Property.PropertyTypeId == request.PropertyTypeId.Value);
        }

        // ----------------------------------------------
        // PRICE FILTER
        // ----------------------------------------------

        if (request.MinPrice.HasValue)
        {
            query = query.Where(x =>
                x.RentPrice >= request.MinPrice.Value);
        }

        if (request.MaxPrice.HasValue)
        {
            query = query.Where(x =>
                x.RentPrice <= request.MaxPrice.Value);
        }

        // ----------------------------------------------
        // AREA FILTER
        // ----------------------------------------------

        if (request.MinArea.HasValue)
        {
            query = query.Where(x =>
                x.AreaM2 >= request.MinArea.Value);
        }

        if (request.MaxArea.HasValue)
        {
            query = query.Where(x =>
                x.AreaM2 <= request.MaxArea.Value);
        }

        // ----------------------------------------------
        // OCCUPANTS, GENDER, AVAILABILITY
        // ----------------------------------------------

        if (request.MinOccupants.HasValue)
        {
            query = query.Where(x =>
                x.MaxOccupants >= request.MinOccupants.Value);
        }

        if (!string.IsNullOrWhiteSpace(genderPreference))
        {
            query = query.Where(x =>
                x.GenderPreference == genderPreference);
        }

        if (!string.IsNullOrWhiteSpace(availabilityStatus))
        {
            query = query.Where(x =>
                x.AvailabilityStatus == availabilityStatus);
        }

        // ----------------------------------------------
        // AMENITIES
        // Room must contain ALL requested amenities.
        // ----------------------------------------------

        foreach (var amenityId in amenityIds)
        {
            var currentAmenityId = amenityId;

            query = query.Where(x =>
                x.Amenities.Any(a =>
                    a.AmenityId == currentAmenityId &&
                    a.IsActive));
        }

        // ----------------------------------------------
        // UNIVERSITY DISTANCE FILTER
        // Approximate straight-line distance, in km.
        // ----------------------------------------------

        if (filterByUniversity)
        {
            var minLatitude =
                (decimal)(universityLatitude - radiusKm / 111.32);

            var maxLatitude =
                (decimal)(universityLatitude + radiusKm / 111.32);

            var minLongitude =
                (decimal)(universityLongitude - radiusKm / longitudeFactor);

            var maxLongitude =
                (decimal)(universityLongitude + radiusKm / longitudeFactor);

            var radiusSquared = radiusKm * radiusKm;

            query = query.Where(x =>
                x.Property.Latitude.HasValue &&
                x.Property.Longitude.HasValue &&

                x.Property.Latitude.Value >= minLatitude &&
                x.Property.Latitude.Value <= maxLatitude &&

                x.Property.Longitude.Value >= minLongitude &&
                x.Property.Longitude.Value <= maxLongitude &&

                (
                    Math.Pow(
                        ((double)x.Property.Latitude.Value
                            - universityLatitude) * 111.32,
                        2.0)
                    +
                    Math.Pow(
                        ((double)x.Property.Longitude.Value
                            - universityLongitude) * longitudeFactor,
                        2.0)
                ) <= radiusSquared);
        }

        // ----------------------------------------------
        // TOTAL COUNT BEFORE PAGINATION
        // ----------------------------------------------

        var totalCount = await query.CountAsync(
            cancellationToken);

        // ----------------------------------------------
        // SORT
        // ----------------------------------------------

        IOrderedQueryable<Room> orderedQuery;

        switch (sortBy)
        {
            case "oldest":
                orderedQuery = query
                    .OrderBy(x => x.PublishedAt ?? x.CreatedAt)
                    .ThenBy(x => x.RoomId);
                break;

            case "price_asc":
                orderedQuery = query
                    .OrderBy(x => x.RentPrice)
                    .ThenBy(x => x.RoomId);
                break;

            case "price_desc":
                orderedQuery = query
                    .OrderByDescending(x => x.RentPrice)
                    .ThenBy(x => x.RoomId);
                break;

            case "area_asc":
                orderedQuery = query
                    .OrderBy(x => x.AreaM2)
                    .ThenBy(x => x.RoomId);
                break;

            case "area_desc":
                orderedQuery = query
                    .OrderByDescending(x => x.AreaM2)
                    .ThenBy(x => x.RoomId);
                break;

            case "distance":
                orderedQuery = query
                    .OrderBy(x =>
                        Math.Pow(
                            ((double)x.Property.Latitude!.Value
                                - universityLatitude) * 111.32,
                            2.0)
                        +
                        Math.Pow(
                            ((double)x.Property.Longitude!.Value
                                - universityLongitude) * longitudeFactor,
                            2.0))
                    .ThenBy(x => x.RoomId);
                break;

            default:
                orderedQuery = query
                    .OrderByDescending(
                        x => x.PublishedAt ?? x.CreatedAt)
                    .ThenByDescending(x => x.RoomId);
                break;
        }

        // ----------------------------------------------
        // PAGINATION AND PROJECTION
        // ----------------------------------------------

        var skip = (request.Page - 1) * request.PageSize;

        var items = await orderedQuery
            .Skip(skip)
            .Take(request.PageSize)
            .Select(x => new RoomCardResponse
            {
                RoomId = x.RoomId,

                PropertyId = x.PropertyId,

                PropertyName = x.Property.PropertyName,

                PropertyStatus = x.Property.Status,

                PropertyTypeId = x.Property.PropertyTypeId,

                PropertyTypeName =
                    x.Property.PropertyType.TypeName,

                Title = x.Title,

                AreaM2 = x.AreaM2,

                RentPrice = x.RentPrice,

                DepositAmount = x.DepositAmount,

                MaxOccupants = x.MaxOccupants,

                GenderPreference = x.GenderPreference,

                ListingStatus = x.ListingStatus,

                AvailabilityStatus = x.AvailabilityStatus,

                AvailableFrom = x.AvailableFrom,

                ProvinceId = x.Property.ProvinceId,

                ProvinceName = x.Property.Province.ProvinceName,

                WardId = x.Property.WardId,

                WardName = x.Property.Ward.WardName,

                AddressLine = x.Property.AddressLine,

                Latitude = x.Property.Latitude,

                Longitude = x.Property.Longitude,

                ThumbnailUrl = x.RoomImages
                    .OrderByDescending(i => i.IsPrimary)
                    .ThenBy(i => i.SortOrder)
                    .ThenBy(i => i.ImageId)
                    .Select(i => i.ImageUrl)
                    .FirstOrDefault(),

                PublishedAt = x.PublishedAt
            })
            .ToListAsync(cancellationToken);

        // DistanceKm is calculated only for the current page.
        if (filterByUniversity)
        {
            foreach (var item in items)
            {
                if (!item.Latitude.HasValue ||
                    !item.Longitude.HasValue)
                {
                    continue;
                }

                var latitudeDifference =
                    ((double)item.Latitude.Value -
                        universityLatitude) * 111.32;

                var longitudeDifference =
                    ((double)item.Longitude.Value -
                        universityLongitude) * longitudeFactor;

                item.DistanceKm = Math.Round(
                    Math.Sqrt(
                        latitudeDifference * latitudeDifference +
                        longitudeDifference * longitudeDifference),
                    2);
            }
        }

        var totalPages = (int)Math.Ceiling(
            totalCount / (double)request.PageSize);

        return new PagedResponse<RoomCardResponse>
        {
            Items = items,

            TotalCount = totalCount,

            Page = request.Page,

            PageSize = request.PageSize,

            TotalPages = totalPages,

            HasNextPage = request.Page < totalPages,

            HasPreviousPage = request.Page > 1
        };
    }

    // ==================================================
    // GET ROOM DETAIL
    // ==================================================

    public async Task<RoomDetailResponse>
        GetRoomDetailAsync(
            long roomId,
            CancellationToken cancellationToken = default)
    {
        var result = await _dbContext.Rooms
            .AsNoTracking()
            .AsSplitQuery()
            .Where(x =>
                x.RoomId == roomId &&
                x.ListingStatus == "PUBLISHED" &&
                x.Property.Status == "ACTIVE" &&
                x.Property.PropertyType.IsActive)
            .Select(x => new RoomDetailResponse
            {
                RoomId = x.RoomId,

                PropertyId = x.PropertyId,

                PropertyName = x.Property.PropertyName,

                PropertyDescription = x.Property.Description,

                PropertyTypeId = x.Property.PropertyTypeId,

                PropertyTypeName =
                    x.Property.PropertyType.TypeName,

                PropertyStatus = x.Property.Status,

                RoomNumber = x.RoomNumber,

                Title = x.Title,

                Description = x.Description,

                AreaM2 = x.AreaM2,

                RentPrice = x.RentPrice,

                DepositAmount = x.DepositAmount,

                ElectricityPrice = x.ElectricityPrice,

                WaterPrice = x.WaterPrice,

                InternetPrice = x.InternetPrice,

                ServiceFee = x.ServiceFee,

                ParkingFee = x.ParkingFee,

                MaxOccupants = x.MaxOccupants,

                GenderPreference = x.GenderPreference,

                AvailableFrom = x.AvailableFrom,

                ListingStatus = x.ListingStatus,

                AvailabilityStatus = x.AvailabilityStatus,

                ProvinceId = x.Property.ProvinceId,

                ProvinceName = x.Property.Province.ProvinceName,

                WardId = x.Property.WardId,

                WardName = x.Property.Ward.WardName,

                AddressLine = x.Property.AddressLine,

                Latitude = x.Property.Latitude,

                Longitude = x.Property.Longitude,

                LandlordIsVerified =
                    x.Property.Landlord.IsVerified,

                ViewCount = x.ViewCount,

                PublishedAt = x.PublishedAt,

                Images = x.RoomImages
                    .OrderByDescending(i => i.IsPrimary)
                    .ThenBy(i => i.SortOrder)
                    .ThenBy(i => i.ImageId)
                    .Select(i => new PublicRoomImageResponse
                    {
                        ImageId = i.ImageId,

                        ImageUrl = i.ImageUrl,

                        IsPrimary = i.IsPrimary,

                        SortOrder = i.SortOrder
                    })
                    .ToList(),

                Amenities = x.Amenities
                    .Where(a => a.IsActive)
                    .OrderBy(a => a.AmenityName)
                    .Select(a => new PublicRoomAmenityResponse
                    {
                        AmenityId = a.AmenityId,

                        AmenityName = a.AmenityName,

                        IconUrl = a.IconUrl
                    })
                    .ToList()
            })
            .SingleOrDefaultAsync(cancellationToken);

        if (result is null)
        {
            throw new NotFoundException(
                "ROOM_NOT_FOUND",
                "Không tìm thấy phòng.");
        }

        return result;
    }

    // ==================================================
    // VALIDATE SEARCH FILTERS
    // ==================================================

    private static void ValidateSearchRequest(
        SearchRoomsRequest request)
    {
        if (request.Page < 1 || request.Page > 100000)
        {
            throw new BadRequestException(
                "INVALID_PAGE",
                "Page phải nằm trong khoảng từ 1 đến 100000.");
        }

        if (request.PageSize < 1 || request.PageSize > 50)
        {
            throw new BadRequestException(
                "INVALID_PAGE_SIZE",
                "PageSize phải nằm trong khoảng từ 1 đến 50.");
        }

        if (request.Keyword?.Trim().Length > 100)
        {
            throw new BadRequestException(
                "INVALID_KEYWORD",
                "Từ khóa không được vượt quá 100 ký tự.");
        }

        if (request.ProvinceId.HasValue &&
            request.ProvinceId.Value <= 0)
        {
            throw new BadRequestException(
                "INVALID_PROVINCE_ID",
                "ProvinceId không hợp lệ.");
        }

        if (request.WardId.HasValue &&
            !request.ProvinceId.HasValue)
        {
            throw new BadRequestException(
                "PROVINCE_REQUIRED",
                "Phải cung cấp ProvinceId khi lọc theo WardId.");
        }

        if (request.WardId.HasValue &&
            request.WardId.Value <= 0)
        {
            throw new BadRequestException(
                "INVALID_WARD_ID",
                "WardId không hợp lệ.");
        }

        if (request.PropertyTypeId.HasValue &&
            request.PropertyTypeId.Value <= 0)
        {
            throw new BadRequestException(
                "INVALID_PROPERTY_TYPE_ID",
                "PropertyTypeId không hợp lệ.");
        }

        if (request.MinPrice < 0 ||
            request.MaxPrice < 0 ||
            (request.MinPrice.HasValue &&
             request.MaxPrice.HasValue &&
             request.MinPrice.Value > request.MaxPrice.Value))
        {
            throw new BadRequestException(
                "INVALID_PRICE_RANGE",
                "Khoảng giá thuê không hợp lệ.");
        }

        if (request.MinArea < 0 ||
            request.MaxArea <= 0 ||
            (request.MinArea.HasValue &&
             request.MaxArea.HasValue &&
             request.MinArea.Value > request.MaxArea.Value))
        {
            throw new BadRequestException(
                "INVALID_AREA_RANGE",
                "Khoảng diện tích không hợp lệ.");
        }

        if (request.MinOccupants.HasValue &&
            request.MinOccupants.Value <= 0)
        {
            throw new BadRequestException(
                "INVALID_MIN_OCCUPANTS",
                "Số người ở phải lớn hơn 0.");
        }

        var gender = request.GenderPreference?
            .Trim()
            .ToUpperInvariant();

        if (!string.IsNullOrWhiteSpace(gender) &&
            gender != "ALL" &&
            gender != "MALE" &&
            gender != "FEMALE")
        {
            throw new BadRequestException(
                "INVALID_GENDER_PREFERENCE",
                "Giới tính ưu tiên không hợp lệ.");
        }

        var availability = request.AvailabilityStatus?
            .Trim()
            .ToUpperInvariant();

        if (!string.IsNullOrWhiteSpace(availability) &&
            availability != "AVAILABLE" &&
            availability != "RESERVED" &&
            availability != "RENTED")
        {
            throw new BadRequestException(
                "INVALID_AVAILABILITY_STATUS",
                "Trạng thái phòng không hợp lệ.");
        }

        var amenityIds = request.AmenityIds ?? [];

        if (amenityIds.Count > 20 ||
            amenityIds.Any(x => x <= 0))
        {
            throw new BadRequestException(
                "INVALID_AMENITY",
                "Danh sách tiện ích không hợp lệ.");
        }

        if (request.RadiusKm.HasValue &&
            (!double.IsFinite(request.RadiusKm.Value) ||
             request.RadiusKm.Value <= 0 ||
             request.RadiusKm.Value > 50))
        {
            throw new BadRequestException(
                "INVALID_RADIUS",
                "Bán kính phải lớn hơn 0 và không vượt quá 50 km.");
        }

        var sortBy = (request.SortBy ?? "newest")
            .Trim()
            .ToLowerInvariant();

        var validSortValues = new[]
        {
            "newest",
            "oldest",
            "price_asc",
            "price_desc",
            "area_asc",
            "area_desc",
            "distance"
        };

        if (!validSortValues.Contains(sortBy))
        {
            throw new BadRequestException(
                "INVALID_SORT_BY",
                "Kiểu sắp xếp không hợp lệ.");
        }

        if (!request.UniversityId.HasValue &&
            (request.RadiusKm.HasValue || sortBy == "distance"))
        {
            throw new BadRequestException(
                "UNIVERSITY_REQUIRED",
                "Phải chọn trường đại học khi lọc hoặc sắp xếp theo khoảng cách.");
        }
    }
}
