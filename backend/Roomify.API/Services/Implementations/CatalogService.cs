using Microsoft.EntityFrameworkCore;

using Roomify.API.Data;
using Roomify.API.DTOs.Catalog;
using Roomify.API.Exceptions;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Services.Implementations;

public class CatalogService : ICatalogService
{
    private readonly RoomifyDbContext _dbContext;

    public CatalogService(
        RoomifyDbContext dbContext)
    {
        _dbContext = dbContext;
    }

    // ======================================================
    // PROVINCES
    // ======================================================

    public async Task<IReadOnlyList<ProvinceResponse>>
        GetProvincesAsync(
            CancellationToken cancellationToken = default)
    {
        return await _dbContext.Provinces
            .AsNoTracking()
            .Where(x => x.IsActive)
            .OrderBy(x => x.ProvinceName)
            .Select(x => new ProvinceResponse
            {
                ProvinceId =
                    x.ProvinceId,

                ProvinceCode =
                    x.ProvinceCode,

                ProvinceName =
                    x.ProvinceName,

                ProvinceType =
                    x.ProvinceType
            })
            .ToListAsync(
                cancellationToken);
    }

    // ======================================================
    // WARDS BY PROVINCE
    // ======================================================

    public async Task<IReadOnlyList<WardResponse>>
        GetWardsByProvinceAsync(
            int provinceId,
            CancellationToken cancellationToken = default)
    {
        var provinceExists =
            await _dbContext.Provinces
                .AsNoTracking()
                .AnyAsync(
                    x =>
                        x.ProvinceId == provinceId
                        &&
                        x.IsActive,
                    cancellationToken);

        if (!provinceExists)
        {
            throw new NotFoundException(
                "PROVINCE_NOT_FOUND",
                "Tỉnh/thành phố không tồn tại hoặc đã ngừng hoạt động.");
        }

        return await _dbContext.Wards
            .AsNoTracking()
            .Where(
                x =>
                    x.ProvinceId == provinceId
                    &&
                    x.IsActive)
            .OrderBy(x => x.WardName)
            .Select(x => new WardResponse
            {
                WardId =
                    x.WardId,

                ProvinceId =
                    x.ProvinceId,

                WardCode =
                    x.WardCode,

                WardName =
                    x.WardName,

                WardType =
                    x.WardType
            })
            .ToListAsync(
                cancellationToken);
    }

    // ======================================================
    // UNIVERSITIES
    // ======================================================

    public async Task<IReadOnlyList<UniversityResponse>>
        GetUniversitiesAsync(
            int? provinceId,
            int? wardId,
            string? keyword,
            CancellationToken cancellationToken = default)
    {
        // Ward chỉ có ý nghĩa khi có Province
        if (wardId.HasValue &&
            !provinceId.HasValue)
        {
            throw new BadRequestException(
                "PROVINCE_REQUIRED",
                "Phải cung cấp ProvinceID khi lọc theo WardID.");
        }

        if (provinceId.HasValue)
        {
            var provinceExists =
                await _dbContext.Provinces
                    .AsNoTracking()
                    .AnyAsync(
                        x =>
                            x.ProvinceId ==
                                provinceId.Value
                            &&
                            x.IsActive,
                        cancellationToken);

            if (!provinceExists)
            {
                throw new NotFoundException(
                    "PROVINCE_NOT_FOUND",
                    "Tỉnh/thành phố không tồn tại hoặc đã ngừng hoạt động.");
            }
        }

        if (provinceId.HasValue &&
            wardId.HasValue)
        {
            var wardExists =
                await _dbContext.Wards
                    .AsNoTracking()
                    .AnyAsync(
                        x =>
                            x.WardId ==
                                wardId.Value
                            &&
                            x.ProvinceId ==
                                provinceId.Value
                            &&
                            x.IsActive,
                        cancellationToken);

            if (!wardExists)
            {
                throw new NotFoundException(
                    "WARD_NOT_FOUND",
                    "Xã/phường không tồn tại trong tỉnh/thành phố đã chọn.");
            }
        }

        var query =
            _dbContext.Universities
                .AsNoTracking()
                .Where(x => x.IsActive)
                .AsQueryable();

        if (provinceId.HasValue)
        {
            query =
                query.Where(
                    x =>
                        x.ProvinceId ==
                        provinceId.Value);
        }

        if (wardId.HasValue)
        {
            query =
                query.Where(
                    x =>
                        x.WardId ==
                        wardId.Value);
        }

        var normalizedKeyword =
            string.IsNullOrWhiteSpace(keyword)
                ? null
                : keyword.Trim();

        if (normalizedKeyword is not null)
        {
            query =
                query.Where(
                    x =>
                        EF.Functions.Like(
                            x.UniversityName,
                            $"%{normalizedKeyword}%"));
        }

        return await query
            .OrderBy(x => x.UniversityName)
            .Select(x => new UniversityResponse
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
                    x.Longitude
            })
            .ToListAsync(
                cancellationToken);
    }

    // ======================================================
    // PROPERTY TYPES
    // ======================================================

    public async Task<IReadOnlyList<PropertyTypeResponse>>
        GetPropertyTypesAsync(
            CancellationToken cancellationToken = default)
    {
        return await _dbContext.PropertyTypes
            .AsNoTracking()
            .Where(x => x.IsActive)
            .OrderBy(x => x.TypeName)
            .Select(x => new PropertyTypeResponse
            {
                PropertyTypeId =
                    x.PropertyTypeId,

                TypeName =
                    x.TypeName
            })
            .ToListAsync(
                cancellationToken);
    }

    // ======================================================
    // AMENITIES
    // ======================================================

    public async Task<IReadOnlyList<AmenityResponse>>
        GetAmenitiesAsync(
            CancellationToken cancellationToken = default)
    {
        return await _dbContext.Amenities
            .AsNoTracking()
            .Where(x => x.IsActive)
            .OrderBy(x => x.AmenityName)
            .Select(x => new AmenityResponse
            {
                AmenityId =
                    x.AmenityId,

                AmenityName =
                    x.AmenityName,

                IconUrl =
                    x.IconUrl
            })
            .ToListAsync(
                cancellationToken);
    }
}