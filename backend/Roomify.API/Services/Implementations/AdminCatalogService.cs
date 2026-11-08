using Microsoft.EntityFrameworkCore;
using Roomify.API.Data;
using Roomify.API.DTOs.Admin.Catalog;
using Roomify.API.Entities;
using Roomify.API.Exceptions;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Services.Implementations;

public class AdminCatalogService : IAdminCatalogService
{
    private readonly RoomifyDbContext _db;

    public AdminCatalogService(RoomifyDbContext db)
    {
        _db = db;
    }

    // ============================================================
    // PROPERTY TYPES - LIST
    // ============================================================

    public async Task<List<AdminPropertyTypeResponse>>
        GetPropertyTypesAsync()
    {
        return await _db.PropertyTypes
            .AsNoTracking()
            .OrderBy(x => x.TypeName)
            .Select(x => new AdminPropertyTypeResponse
            {
                PropertyTypeId = x.PropertyTypeId,
                TypeName = x.TypeName,
                IsActive = x.IsActive
            })
            .ToListAsync();
    }

    // ============================================================
    // PROPERTY TYPES - CREATE
    // ============================================================

    public async Task<AdminPropertyTypeResponse>
        CreatePropertyTypeAsync(
            SavePropertyTypeRequest request)
    {
        var typeName =
            NormalizeRequired(request.TypeName);

        if (string.IsNullOrWhiteSpace(typeName))
        {
            throw new BadRequestException(
                "PROPERTY_TYPE_NAME_REQUIRED",
                "Tên loại hình nhà trọ không được để trống.");
        }

        var exists = await _db.PropertyTypes
            .AnyAsync(x =>
                x.TypeName == typeName);

        if (exists)
        {
            throw new ConflictException(
                "PROPERTY_TYPE_ALREADY_EXISTS",
                "Loại hình nhà trọ đã tồn tại.");
        }

        var entity = new PropertyType
        {
            TypeName = typeName,
            IsActive = request.IsActive
        };

        _db.PropertyTypes.Add(entity);

        await _db.SaveChangesAsync();

        return MapPropertyType(entity);
    }

    // ============================================================
    // PROPERTY TYPES - UPDATE
    // ============================================================

    public async Task<AdminPropertyTypeResponse>
        UpdatePropertyTypeAsync(
            int propertyTypeId,
            SavePropertyTypeRequest request)
    {
        var entity = await _db.PropertyTypes
            .FirstOrDefaultAsync(x =>
                x.PropertyTypeId == propertyTypeId);

        if (entity is null)
        {
            throw new NotFoundException(
                "PROPERTY_TYPE_NOT_FOUND",
                "Không tìm thấy loại hình nhà trọ.");
        }

        var typeName =
            NormalizeRequired(request.TypeName);

        if (string.IsNullOrWhiteSpace(typeName))
        {
            throw new BadRequestException(
                "PROPERTY_TYPE_NAME_REQUIRED",
                "Tên loại hình nhà trọ không được để trống.");
        }

        var duplicated = await _db.PropertyTypes
            .AnyAsync(x =>
                x.PropertyTypeId != propertyTypeId &&
                x.TypeName == typeName);

        if (duplicated)
        {
            throw new ConflictException(
                "PROPERTY_TYPE_ALREADY_EXISTS",
                "Loại hình nhà trọ đã tồn tại.");
        }

        entity.TypeName = typeName;
        entity.IsActive = request.IsActive;

        await _db.SaveChangesAsync();

        return MapPropertyType(entity);
    }

    // ============================================================
    // PROPERTY TYPES - ACTIVATE
    // ============================================================

    public async Task<AdminPropertyTypeResponse>
        ActivatePropertyTypeAsync(
            int propertyTypeId)
    {
        var entity = await GetPropertyTypeEntityAsync(
            propertyTypeId);

        if (entity.IsActive)
        {
            throw new ConflictException(
                "PROPERTY_TYPE_ALREADY_ACTIVE",
                "Loại hình nhà trọ hiện đã hoạt động.");
        }

        entity.IsActive = true;

        await _db.SaveChangesAsync();

        return MapPropertyType(entity);
    }

    // ============================================================
    // PROPERTY TYPES - DEACTIVATE
    // ============================================================

    public async Task<AdminPropertyTypeResponse>
        DeactivatePropertyTypeAsync(
            int propertyTypeId)
    {
        var entity = await GetPropertyTypeEntityAsync(
            propertyTypeId);

        if (!entity.IsActive)
        {
            throw new ConflictException(
                "PROPERTY_TYPE_ALREADY_INACTIVE",
                "Loại hình nhà trọ hiện đã bị vô hiệu hóa.");
        }

        entity.IsActive = false;

        await _db.SaveChangesAsync();

        return MapPropertyType(entity);
    }


    // ============================================================
    // AMENITIES - LIST
    // ============================================================

    public async Task<List<AdminAmenityResponse>>
        GetAmenitiesAsync()
    {
        return await _db.Amenities
            .AsNoTracking()
            .OrderBy(x => x.AmenityName)
            .Select(x => new AdminAmenityResponse
            {
                AmenityId = x.AmenityId,
                AmenityName = x.AmenityName,
                IconUrl = x.IconUrl,
                IsActive = x.IsActive
            })
            .ToListAsync();
    }

    // ============================================================
    // AMENITIES - CREATE
    // ============================================================

    public async Task<AdminAmenityResponse>
        CreateAmenityAsync(
            SaveAmenityRequest request)
    {
        var amenityName =
            NormalizeRequired(request.AmenityName);

        if (string.IsNullOrWhiteSpace(amenityName))
        {
            throw new BadRequestException(
                "AMENITY_NAME_REQUIRED",
                "Tên tiện ích không được để trống.");
        }

        var exists = await _db.Amenities
            .AnyAsync(x =>
                x.AmenityName == amenityName);

        if (exists)
        {
            throw new ConflictException(
                "AMENITY_ALREADY_EXISTS",
                "Tiện ích đã tồn tại.");
        }

        var entity = new Amenity
        {
            AmenityName = amenityName,
            IconUrl = NormalizeOptional(request.IconUrl),
            IsActive = request.IsActive
        };

        _db.Amenities.Add(entity);

        await _db.SaveChangesAsync();

        return MapAmenity(entity);
    }

    // ============================================================
    // AMENITIES - UPDATE
    // ============================================================

    public async Task<AdminAmenityResponse>
        UpdateAmenityAsync(
            int amenityId,
            SaveAmenityRequest request)
    {
        var entity = await _db.Amenities
            .FirstOrDefaultAsync(x =>
                x.AmenityId == amenityId);

        if (entity is null)
        {
            throw new NotFoundException(
                "AMENITY_NOT_FOUND",
                "Không tìm thấy tiện ích.");
        }

        var amenityName =
            NormalizeRequired(request.AmenityName);

        if (string.IsNullOrWhiteSpace(amenityName))
        {
            throw new BadRequestException(
                "AMENITY_NAME_REQUIRED",
                "Tên tiện ích không được để trống.");
        }

        var duplicated = await _db.Amenities
            .AnyAsync(x =>
                x.AmenityId != amenityId &&
                x.AmenityName == amenityName);

        if (duplicated)
        {
            throw new ConflictException(
                "AMENITY_ALREADY_EXISTS",
                "Tiện ích đã tồn tại.");
        }

        entity.AmenityName = amenityName;

        entity.IconUrl =
            NormalizeOptional(request.IconUrl);

        entity.IsActive =
            request.IsActive;

        await _db.SaveChangesAsync();

        return MapAmenity(entity);
    }

    // ============================================================
    // AMENITIES - ACTIVATE
    // ============================================================

    public async Task<AdminAmenityResponse>
        ActivateAmenityAsync(
            int amenityId)
    {
        var entity = await GetAmenityEntityAsync(
            amenityId);

        if (entity.IsActive)
        {
            throw new ConflictException(
                "AMENITY_ALREADY_ACTIVE",
                "Tiện ích hiện đã hoạt động.");
        }

        entity.IsActive = true;

        await _db.SaveChangesAsync();

        return MapAmenity(entity);
    }

    // ============================================================
    // AMENITIES - DEACTIVATE
    // ============================================================

    public async Task<AdminAmenityResponse>
        DeactivateAmenityAsync(
            int amenityId)
    {
        var entity = await GetAmenityEntityAsync(
            amenityId);

        if (!entity.IsActive)
        {
            throw new ConflictException(
                "AMENITY_ALREADY_INACTIVE",
                "Tiện ích hiện đã bị vô hiệu hóa.");
        }

        entity.IsActive = false;

        await _db.SaveChangesAsync();

        return MapAmenity(entity);
    }


    // ============================================================
    // UNIVERSITIES - LIST
    // ============================================================

    public async Task<List<AdminUniversityResponse>>
        GetUniversitiesAsync()
    {
        return await ProjectUniversity(
                _db.Universities.AsNoTracking())
            .OrderBy(x => x.UniversityName)
            .ToListAsync();
    }

    // ============================================================
    // UNIVERSITIES - CREATE
    // ============================================================

    public async Task<AdminUniversityResponse>
        CreateUniversityAsync(
            SaveUniversityRequest request)
    {
        var universityName =
            NormalizeRequired(request.UniversityName);

        if (string.IsNullOrWhiteSpace(universityName))
        {
            throw new BadRequestException(
                "UNIVERSITY_NAME_REQUIRED",
                "Tên trường đại học không được để trống.");
        }

        await ValidateUniversityLocationAsync(
            request.ProvinceId,
            request.WardId);

        var entity = new University
        {
            UniversityName = universityName,

            ProvinceId =
                request.ProvinceId,

            WardId =
                request.WardId,

            AddressLine =
                NormalizeOptional(
                    request.AddressLine),

            Latitude =
                request.Latitude,

            Longitude =
                request.Longitude,

            IsActive =
                request.IsActive
        };

        _db.Universities.Add(entity);

        await _db.SaveChangesAsync();

        return await GetUniversityByIdAsync(
            entity.UniversityId);
    }

    // ============================================================
    // UNIVERSITIES - UPDATE
    // ============================================================

    public async Task<AdminUniversityResponse>
        UpdateUniversityAsync(
            long universityId,
            SaveUniversityRequest request)
    {
        var entity = await _db.Universities
            .FirstOrDefaultAsync(x =>
                x.UniversityId == universityId);

        if (entity is null)
        {
            throw new NotFoundException(
                "UNIVERSITY_NOT_FOUND",
                "Không tìm thấy trường đại học.");
        }

        var universityName =
            NormalizeRequired(request.UniversityName);

        if (string.IsNullOrWhiteSpace(universityName))
        {
            throw new BadRequestException(
                "UNIVERSITY_NAME_REQUIRED",
                "Tên trường đại học không được để trống.");
        }

        await ValidateUniversityLocationAsync(
            request.ProvinceId,
            request.WardId);

        entity.UniversityName =
            universityName;

        entity.ProvinceId =
            request.ProvinceId;

        entity.WardId =
            request.WardId;

        entity.AddressLine =
            NormalizeOptional(
                request.AddressLine);

        entity.Latitude =
            request.Latitude;

        entity.Longitude =
            request.Longitude;

        entity.IsActive =
            request.IsActive;

        await _db.SaveChangesAsync();

        return await GetUniversityByIdAsync(
            universityId);
    }

    // ============================================================
    // UNIVERSITIES - ACTIVATE
    // ============================================================

    public async Task<AdminUniversityResponse>
        ActivateUniversityAsync(
            long universityId)
    {
        var entity = await GetUniversityEntityAsync(
            universityId);

        if (entity.IsActive)
        {
            throw new ConflictException(
                "UNIVERSITY_ALREADY_ACTIVE",
                "Trường đại học hiện đã hoạt động.");
        }

        entity.IsActive = true;

        await _db.SaveChangesAsync();

        return await GetUniversityByIdAsync(
            universityId);
    }

    // ============================================================
    // UNIVERSITIES - DEACTIVATE
    // ============================================================

    public async Task<AdminUniversityResponse>
        DeactivateUniversityAsync(
            long universityId)
    {
        var entity = await GetUniversityEntityAsync(
            universityId);

        if (!entity.IsActive)
        {
            throw new ConflictException(
                "UNIVERSITY_ALREADY_INACTIVE",
                "Trường đại học hiện đã bị vô hiệu hóa.");
        }

        entity.IsActive = false;

        await _db.SaveChangesAsync();

        return await GetUniversityByIdAsync(
            universityId);
    }


    // ============================================================
    // PROPERTY TYPE HELPER
    // ============================================================

    private async Task<PropertyType>
        GetPropertyTypeEntityAsync(
            int propertyTypeId)
    {
        var entity = await _db.PropertyTypes
            .FirstOrDefaultAsync(x =>
                x.PropertyTypeId ==
                propertyTypeId);

        if (entity is null)
        {
            throw new NotFoundException(
                "PROPERTY_TYPE_NOT_FOUND",
                "Không tìm thấy loại hình nhà trọ.");
        }

        return entity;
    }

    // ============================================================
    // AMENITY HELPER
    // ============================================================

    private async Task<Amenity>
        GetAmenityEntityAsync(
            int amenityId)
    {
        var entity = await _db.Amenities
            .FirstOrDefaultAsync(x =>
                x.AmenityId ==
                amenityId);

        if (entity is null)
        {
            throw new NotFoundException(
                "AMENITY_NOT_FOUND",
                "Không tìm thấy tiện ích.");
        }

        return entity;
    }

    // ============================================================
    // UNIVERSITY HELPER
    // ============================================================

    private async Task<University>
        GetUniversityEntityAsync(
            long universityId)
    {
        var entity = await _db.Universities
            .FirstOrDefaultAsync(x =>
                x.UniversityId ==
                universityId);

        if (entity is null)
        {
            throw new NotFoundException(
                "UNIVERSITY_NOT_FOUND",
                "Không tìm thấy trường đại học.");
        }

        return entity;
    }

    private async Task<AdminUniversityResponse>
        GetUniversityByIdAsync(
            long universityId)
    {
        var response = await ProjectUniversity(
                _db.Universities
                    .AsNoTracking()
                    .Where(x =>
                        x.UniversityId ==
                        universityId))
            .FirstOrDefaultAsync();

        if (response is null)
        {
            throw new NotFoundException(
                "UNIVERSITY_NOT_FOUND",
                "Không tìm thấy trường đại học.");
        }

        return response;
    }

    // ============================================================
    // UNIVERSITY LOCATION VALIDATION
    // ============================================================

    private async Task ValidateUniversityLocationAsync(
        int? provinceId,
        int? wardId)
    {
        /*
         * Không cho Ward tồn tại mà thiếu Province vì FK của University
         * dùng cặp ProvinceID + WardID.
         */
        if (wardId.HasValue &&
            !provinceId.HasValue)
        {
            throw new BadRequestException(
                "PROVINCE_REQUIRED_FOR_WARD",
                "Phải chọn tỉnh/thành phố khi chọn phường/xã.");
        }

        if (provinceId.HasValue)
        {
            var provinceExists =
                await _db.Provinces.AnyAsync(x =>
                    x.ProvinceId ==
                    provinceId.Value);

            if (!provinceExists)
            {
                throw new BadRequestException(
                    "PROVINCE_NOT_FOUND",
                    "Tỉnh/thành phố không tồn tại.");
            }
        }

        if (wardId.HasValue)
        {
            var wardExists =
                await _db.Wards.AnyAsync(x =>
                    x.ProvinceId ==
                    provinceId!.Value &&
                    x.WardId ==
                    wardId.Value);

            if (!wardExists)
            {
                throw new BadRequestException(
                    "WARD_NOT_FOUND",
                    "Phường/xã không tồn tại hoặc không thuộc tỉnh/thành phố đã chọn.");
            }
        }
    }

    // ============================================================
    // UNIVERSITY PROJECTION
    // ============================================================

    private static IQueryable<AdminUniversityResponse>
        ProjectUniversity(
            IQueryable<University> query)
    {
        return query.Select(x =>
            new AdminUniversityResponse
            {
                UniversityId =
                    x.UniversityId,

                UniversityName =
                    x.UniversityName,

                ProvinceId =
                    x.ProvinceId,

                ProvinceName =
                    x.Province != null
                        ? x.Province.ProvinceName
                        : null,

                WardId =
                    x.WardId,

                WardName =
                    x.Ward != null
                        ? x.Ward.WardName
                        : null,

                AddressLine =
                    x.AddressLine,

                Latitude =
                    x.Latitude,

                Longitude =
                    x.Longitude,

                IsActive =
                    x.IsActive
            });
    }

    // ============================================================
    // MAPPING
    // ============================================================

    private static AdminPropertyTypeResponse
        MapPropertyType(
            PropertyType entity)
    {
        return new AdminPropertyTypeResponse
        {
            PropertyTypeId =
                entity.PropertyTypeId,

            TypeName =
                entity.TypeName,

            IsActive =
                entity.IsActive
        };
    }

    private static AdminAmenityResponse
        MapAmenity(
            Amenity entity)
    {
        return new AdminAmenityResponse
        {
            AmenityId =
                entity.AmenityId,

            AmenityName =
                entity.AmenityName,

            IconUrl =
                entity.IconUrl,

            IsActive =
                entity.IsActive
        };
    }

    // ============================================================
    // NORMALIZATION
    // ============================================================

    private static string NormalizeRequired(
        string value)
    {
        return value.Trim();
    }

    private static string? NormalizeOptional(
        string? value)
    {
        if (string.IsNullOrWhiteSpace(value))
        {
            return null;
        }

        return value.Trim();
    }
}