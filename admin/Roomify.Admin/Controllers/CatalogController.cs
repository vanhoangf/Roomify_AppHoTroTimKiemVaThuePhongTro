using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

using Roomify.Admin.Models.Catalog;
using Roomify.Admin.Services.Catalog;

namespace Roomify.Admin.Controllers;

[Authorize(Roles = "ADMIN")]
public class CatalogController : Controller
{
    private readonly IAdminCatalogApiService
        _catalogService;


    public CatalogController(
        IAdminCatalogApiService catalogService)
    {
        _catalogService =
            catalogService;
    }


    // ============================================================
    // INDEX - PROPERTY TYPES
    // ============================================================

    [HttpGet]
    public async Task<IActionResult> Index(
        string? tab = null,
        string? search = null,
        string? status = null,
        CancellationToken cancellationToken = default)
    {
        var normalizedTab =
            string.IsNullOrWhiteSpace(tab)
                ? "property-types"
                : tab.Trim()
                    .ToLowerInvariant();


        if (normalizedTab is not
            "property-types"
            and not
            "amenities"
            and not
            "universities")
        {
            normalizedTab =
                "property-types";
        }


        var normalizedSearch =
            string.IsNullOrWhiteSpace(search)
                ? null
                : search.Trim();


        var normalizedStatus =
            string.IsNullOrWhiteSpace(status)
                ? null
                : status.Trim()
                    .ToUpperInvariant();


        if (normalizedStatus is not null &&
            normalizedStatus is not "ACTIVE"
            and not "INACTIVE")
        {
            normalizedStatus =
                null;
        }


        var model =
            new AdminCatalogViewModel
            {
                ActiveTab =
                    normalizedTab,

                Search =
                    normalizedSearch,

                Status =
                    normalizedStatus
            };


        // ============================================================
        // PROPERTY TYPES
        // ============================================================

        if (normalizedTab == "property-types")
        {
            var result =
                await _catalogService
                    .GetPropertyTypesAsync(
                        cancellationToken);


            if (!result.IsSuccess &&
                result.StatusCode == 401)
            {
                await SignOutAsync();

                return RedirectToAction(
                    "Login",
                    "Auth");
            }


            if (!result.IsSuccess ||
                result.Data is null)
            {
                model.ErrorMessage =
                    result.Message
                    ?? "Không thể tải danh sách loại hình nhà trọ.";

                return View(model);
            }


            var allItems =
                result.Data;


            model.TotalPropertyTypes =
                allItems.Count;

            model.ActivePropertyTypes =
                allItems.Count(x =>
                    x.IsActive);

            model.InactivePropertyTypes =
                allItems.Count(x =>
                    !x.IsActive);


            IEnumerable<AdminPropertyTypeDto>
                filtered =
                    allItems;


            if (normalizedSearch is not null)
            {
                filtered =
                    filtered.Where(x =>
                        x.TypeName.Contains(
                            normalizedSearch,
                            StringComparison.OrdinalIgnoreCase));
            }


            if (normalizedStatus == "ACTIVE")
            {
                filtered =
                    filtered.Where(x =>
                        x.IsActive);
            }
            else if (normalizedStatus == "INACTIVE")
            {
                filtered =
                    filtered.Where(x =>
                        !x.IsActive);
            }


            model.PropertyTypes =
                filtered
                    .OrderBy(x =>
                        x.TypeName)
                    .ToList();


            return View(model);
        }


        // ============================================================
        // AMENITIES
        // ============================================================

        if (normalizedTab == "amenities")
        {
            var amenityResult =
                await _catalogService
                    .GetAmenitiesAsync(
                        cancellationToken);


            if (!amenityResult.IsSuccess &&
                amenityResult.StatusCode == 401)
            {
                await SignOutAsync();

                return RedirectToAction(
                    "Login",
                    "Auth");
            }


            if (!amenityResult.IsSuccess ||
                amenityResult.Data is null)
            {
                model.ErrorMessage =
                    amenityResult.Message
                    ?? "Không thể tải danh sách tiện ích.";

                return View(model);
            }


            var allAmenities =
                amenityResult.Data;


            model.TotalAmenities =
                allAmenities.Count;

            model.ActiveAmenities =
                allAmenities.Count(x =>
                    x.IsActive);

            model.InactiveAmenities =
                allAmenities.Count(x =>
                    !x.IsActive);


            IEnumerable<AdminAmenityDto>
                filteredAmenities =
                    allAmenities;


            if (normalizedSearch is not null)
            {
                filteredAmenities =
                    filteredAmenities.Where(x =>
                        x.AmenityName.Contains(
                            normalizedSearch,
                            StringComparison.OrdinalIgnoreCase)
                        ||
                        (
                            x.IconUrl != null
                            &&
                            x.IconUrl.Contains(
                                normalizedSearch,
                                StringComparison.OrdinalIgnoreCase)
                        ));
            }


            if (normalizedStatus == "ACTIVE")
            {
                filteredAmenities =
                    filteredAmenities.Where(x =>
                        x.IsActive);
            }
            else if (normalizedStatus == "INACTIVE")
            {
                filteredAmenities =
                    filteredAmenities.Where(x =>
                        !x.IsActive);
            }


            model.Amenities =
                filteredAmenities
                    .OrderBy(x =>
                        x.AmenityName)
                    .ToList();


            return View(model);
        }


        // ============================================================
        // UNIVERSITIES
        // ============================================================

        var universityTask =
            _catalogService.GetUniversitiesAsync(
                cancellationToken);

        var provinceTask =
            _catalogService.GetProvincesAsync(
                cancellationToken);


        await Task.WhenAll(
            universityTask,
            provinceTask);


        var universityResult =
            await universityTask;

        var provinceResult =
            await provinceTask;


        if ((!universityResult.IsSuccess &&
             universityResult.StatusCode == 401)
            ||
            (!provinceResult.IsSuccess &&
             provinceResult.StatusCode == 401))
        {
            await SignOutAsync();

            return RedirectToAction(
                "Login",
                "Auth");
        }


        if (!universityResult.IsSuccess ||
            universityResult.Data is null)
        {
            model.ErrorMessage =
                universityResult.Message
                ?? "Không thể tải danh sách trường đại học.";

            return View(model);
        }


        if (!provinceResult.IsSuccess ||
            provinceResult.Data is null)
        {
            model.ErrorMessage =
                provinceResult.Message
                ?? "Không thể tải danh sách tỉnh/thành phố.";

            return View(model);
        }


        model.Provinces =
            provinceResult.Data
                .OrderBy(x =>
                    x.ProvinceName)
                .ToList();


        var allUniversities =
            universityResult.Data;


        model.TotalUniversities =
            allUniversities.Count;

        model.ActiveUniversities =
            allUniversities.Count(x =>
                x.IsActive);

        model.InactiveUniversities =
            allUniversities.Count(x =>
                !x.IsActive);


        IEnumerable<AdminUniversityDto>
            filteredUniversities =
                allUniversities;


        if (normalizedSearch is not null)
        {
            filteredUniversities =
                filteredUniversities.Where(x =>
                    x.UniversityName.Contains(
                        normalizedSearch,
                        StringComparison.OrdinalIgnoreCase)
                    ||
                    (
                        x.ProvinceName != null
                        &&
                        x.ProvinceName.Contains(
                            normalizedSearch,
                            StringComparison.OrdinalIgnoreCase)
                    )
                    ||
                    (
                        x.WardName != null
                        &&
                        x.WardName.Contains(
                            normalizedSearch,
                            StringComparison.OrdinalIgnoreCase)
                    )
                    ||
                    (
                        x.AddressLine != null
                        &&
                        x.AddressLine.Contains(
                            normalizedSearch,
                            StringComparison.OrdinalIgnoreCase)
                    ));
        }


        if (normalizedStatus == "ACTIVE")
        {
            filteredUniversities =
                filteredUniversities.Where(x =>
                    x.IsActive);
        }
        else if (normalizedStatus == "INACTIVE")
        {
            filteredUniversities =
                filteredUniversities.Where(x =>
                    !x.IsActive);
        }


        model.Universities =
            filteredUniversities
                .OrderBy(x =>
                    x.UniversityName)
                .ToList();


        return View(model);
    }


    // ============================================================
    // CREATE PROPERTY TYPE
    // ============================================================

    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult>
        CreatePropertyType(
            string? typeName,
            bool isActive = true,
            string? returnUrl = null,
            CancellationToken cancellationToken = default)
    {
        var normalizedName =
            typeName?.Trim();


        if (string.IsNullOrWhiteSpace(
                normalizedName))
        {
            TempData["ErrorMessage"] =
                "Vui lòng nhập tên loại hình nhà trọ.";

            return RedirectBack(
                returnUrl);
        }


        if (normalizedName.Length > 100)
        {
            TempData["ErrorMessage"] =
                "Tên loại hình nhà trọ không được vượt quá 100 ký tự.";

            return RedirectBack(
                returnUrl);
        }


        var result =
            await _catalogService
                .CreatePropertyTypeAsync(
                    new SavePropertyTypeRequestDto
                    {
                        TypeName =
                            normalizedName,

                        IsActive =
                            isActive
                    },
                    cancellationToken);


        if (!result.IsSuccess &&
            result.StatusCode == 401)
        {
            await SignOutAsync();

            return RedirectToAction(
                "Login",
                "Auth");
        }


        if (!result.IsSuccess)
        {
            TempData["ErrorMessage"] =
                result.Message
                ?? "Không thể tạo loại hình nhà trọ.";

            return RedirectBack(
                returnUrl);
        }


        TempData["SuccessMessage"] =
            "Đã thêm loại hình nhà trọ mới.";


        return RedirectBack(
            returnUrl);
    }


    // ============================================================
    // UPDATE PROPERTY TYPE
    // ============================================================

    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult>
        UpdatePropertyType(
            int propertyTypeId,
            string? typeName,
            bool isActive,
            string? returnUrl = null,
            CancellationToken cancellationToken = default)
    {
        if (propertyTypeId <= 0)
        {
            TempData["ErrorMessage"] =
                "Loại hình nhà trọ không hợp lệ.";

            return RedirectBack(
                returnUrl);
        }


        var normalizedName =
            typeName?.Trim();


        if (string.IsNullOrWhiteSpace(
                normalizedName))
        {
            TempData["ErrorMessage"] =
                "Vui lòng nhập tên loại hình nhà trọ.";

            return RedirectBack(
                returnUrl);
        }


        if (normalizedName.Length > 100)
        {
            TempData["ErrorMessage"] =
                "Tên loại hình nhà trọ không được vượt quá 100 ký tự.";

            return RedirectBack(
                returnUrl);
        }


        var result =
            await _catalogService
                .UpdatePropertyTypeAsync(
                    propertyTypeId,
                    new SavePropertyTypeRequestDto
                    {
                        TypeName =
                            normalizedName,

                        IsActive =
                            isActive
                    },
                    cancellationToken);


        if (!result.IsSuccess &&
            result.StatusCode == 401)
        {
            await SignOutAsync();

            return RedirectToAction(
                "Login",
                "Auth");
        }


        if (!result.IsSuccess)
        {
            TempData["ErrorMessage"] =
                result.Message
                ?? "Không thể cập nhật loại hình nhà trọ.";

            return RedirectBack(
                returnUrl);
        }


        TempData["SuccessMessage"] =
            "Đã cập nhật loại hình nhà trọ.";


        return RedirectBack(
            returnUrl);
    }


    // ============================================================
    // ACTIVATE
    // ============================================================

    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult>
        ActivatePropertyType(
            int propertyTypeId,
            string? returnUrl = null,
            CancellationToken cancellationToken = default)
    {
        var result =
            await _catalogService
                .ActivatePropertyTypeAsync(
                    propertyTypeId,
                    cancellationToken);


        if (!result.IsSuccess &&
            result.StatusCode == 401)
        {
            await SignOutAsync();

            return RedirectToAction(
                "Login",
                "Auth");
        }


        if (!result.IsSuccess)
        {
            TempData["ErrorMessage"] =
                result.Message
                ?? "Không thể kích hoạt loại hình nhà trọ.";

            return RedirectBack(
                returnUrl);
        }


        TempData["SuccessMessage"] =
            "Loại hình nhà trọ đã được kích hoạt.";


        return RedirectBack(
            returnUrl);
    }


    // ============================================================
    // DEACTIVATE
    // ============================================================

    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult>
        DeactivatePropertyType(
            int propertyTypeId,
            string? returnUrl = null,
            CancellationToken cancellationToken = default)
    {
        var result =
            await _catalogService
                .DeactivatePropertyTypeAsync(
                    propertyTypeId,
                    cancellationToken);


        if (!result.IsSuccess &&
            result.StatusCode == 401)
        {
            await SignOutAsync();

            return RedirectToAction(
                "Login",
                "Auth");
        }


        if (!result.IsSuccess)
        {
            TempData["ErrorMessage"] =
                result.Message
                ?? "Không thể vô hiệu hóa loại hình nhà trọ.";

            return RedirectBack(
                returnUrl);
        }


        TempData["SuccessMessage"] =
            "Loại hình nhà trọ đã được vô hiệu hóa.";


        return RedirectBack(
            returnUrl);
    }

    // ============================================================
    // CREATE AMENITY
    // ============================================================

    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> CreateAmenity(
        string? amenityName,
        string? iconUrl,
        bool isActive = true,
        string? returnUrl = null,
        CancellationToken cancellationToken = default)
    {
        var normalizedName =
            amenityName?.Trim();


        var normalizedIconUrl =
            string.IsNullOrWhiteSpace(iconUrl)
                ? null
                : iconUrl.Trim();


        if (string.IsNullOrWhiteSpace(
                normalizedName))
        {
            TempData["ErrorMessage"] =
                "Vui lòng nhập tên tiện ích.";

            return RedirectBack(
                returnUrl);
        }


        if (normalizedName.Length > 100)
        {
            TempData["ErrorMessage"] =
                "Tên tiện ích không được vượt quá 100 ký tự.";

            return RedirectBack(
                returnUrl);
        }


        if (normalizedIconUrl is not null &&
            normalizedIconUrl.Length > 500)
        {
            TempData["ErrorMessage"] =
                "Đường dẫn icon không được vượt quá 500 ký tự.";

            return RedirectBack(
                returnUrl);
        }


        var result =
            await _catalogService
                .CreateAmenityAsync(
                    new SaveAmenityRequestDto
                    {
                        AmenityName =
                            normalizedName,

                        IconUrl =
                            normalizedIconUrl,

                        IsActive =
                            isActive
                    },
                    cancellationToken);


        if (!result.IsSuccess &&
            result.StatusCode == 401)
        {
            await SignOutAsync();

            return RedirectToAction(
                "Login",
                "Auth");
        }


        if (!result.IsSuccess)
        {
            TempData["ErrorMessage"] =
                result.Message
                ?? "Không thể tạo tiện ích.";

            return RedirectBack(
                returnUrl);
        }


        TempData["SuccessMessage"] =
            "Đã thêm tiện ích mới.";


        return RedirectBack(
            returnUrl);
    }


    // ============================================================
    // UPDATE AMENITY
    // ============================================================

    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> UpdateAmenity(
        int amenityId,
        string? amenityName,
        string? iconUrl,
        bool isActive,
        string? returnUrl = null,
        CancellationToken cancellationToken = default)
    {
        if (amenityId <= 0)
        {
            TempData["ErrorMessage"] =
                "Tiện ích không hợp lệ.";

            return RedirectBack(
                returnUrl);
        }


        var normalizedName =
            amenityName?.Trim();


        var normalizedIconUrl =
            string.IsNullOrWhiteSpace(iconUrl)
                ? null
                : iconUrl.Trim();


        if (string.IsNullOrWhiteSpace(
                normalizedName))
        {
            TempData["ErrorMessage"] =
                "Vui lòng nhập tên tiện ích.";

            return RedirectBack(
                returnUrl);
        }


        if (normalizedName.Length > 100)
        {
            TempData["ErrorMessage"] =
                "Tên tiện ích không được vượt quá 100 ký tự.";

            return RedirectBack(
                returnUrl);
        }


        if (normalizedIconUrl is not null &&
            normalizedIconUrl.Length > 500)
        {
            TempData["ErrorMessage"] =
                "Đường dẫn icon không được vượt quá 500 ký tự.";

            return RedirectBack(
                returnUrl);
        }


        var result =
            await _catalogService
                .UpdateAmenityAsync(
                    amenityId,
                    new SaveAmenityRequestDto
                    {
                        AmenityName =
                            normalizedName,

                        IconUrl =
                            normalizedIconUrl,

                        IsActive =
                            isActive
                    },
                    cancellationToken);


        if (!result.IsSuccess &&
            result.StatusCode == 401)
        {
            await SignOutAsync();

            return RedirectToAction(
                "Login",
                "Auth");
        }


        if (!result.IsSuccess)
        {
            TempData["ErrorMessage"] =
                result.Message
                ?? "Không thể cập nhật tiện ích.";

            return RedirectBack(
                returnUrl);
        }


        TempData["SuccessMessage"] =
            "Đã cập nhật tiện ích.";


        return RedirectBack(
            returnUrl);
    }


    // ============================================================
    // ACTIVATE AMENITY
    // ============================================================

    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> ActivateAmenity(
        int amenityId,
        string? returnUrl = null,
        CancellationToken cancellationToken = default)
    {
        var result =
            await _catalogService
                .ActivateAmenityAsync(
                    amenityId,
                    cancellationToken);


        if (!result.IsSuccess &&
            result.StatusCode == 401)
        {
            await SignOutAsync();

            return RedirectToAction(
                "Login",
                "Auth");
        }


        if (!result.IsSuccess)
        {
            TempData["ErrorMessage"] =
                result.Message
                ?? "Không thể kích hoạt tiện ích.";

            return RedirectBack(
                returnUrl);
        }


        TempData["SuccessMessage"] =
            "Tiện ích đã được kích hoạt.";


        return RedirectBack(
            returnUrl);
    }


    // ============================================================
    // DEACTIVATE AMENITY
    // ============================================================

    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> DeactivateAmenity(
        int amenityId,
        string? returnUrl = null,
        CancellationToken cancellationToken = default)
    {
        var result =
            await _catalogService
                .DeactivateAmenityAsync(
                    amenityId,
                    cancellationToken);


        if (!result.IsSuccess &&
            result.StatusCode == 401)
        {
            await SignOutAsync();

            return RedirectToAction(
                "Login",
                "Auth");
        }


        if (!result.IsSuccess)
        {
            TempData["ErrorMessage"] =
                result.Message
                ?? "Không thể vô hiệu hóa tiện ích.";

            return RedirectBack(
                returnUrl);
        }


        TempData["SuccessMessage"] =
            "Tiện ích đã được vô hiệu hóa.";


        return RedirectBack(
            returnUrl);
    }
    // ============================================================
    // WARDS BY PROVINCE
    // ============================================================

    [HttpGet]
    public async Task<IActionResult> Wards(
        int provinceId,
        CancellationToken cancellationToken = default)
    {
        if (provinceId <= 0)
        {
            return BadRequest(
                new
                {
                    message =
                        "Tỉnh/thành phố không hợp lệ."
                });
        }


        var result =
            await _catalogService
                .GetWardsByProvinceAsync(
                    provinceId,
                    cancellationToken);


        if (!result.IsSuccess)
        {
            if (result.StatusCode == 401)
            {
                await SignOutAsync();

                return Unauthorized(
                    new
                    {
                        message =
                            "Phiên đăng nhập đã hết hạn."
                    });
            }


            return StatusCode(
                result.StatusCode ?? 500,
                new
                {
                    message =
                        result.Message
                        ?? "Không thể tải danh sách phường/xã."
                });
        }


        return Json(
            result.Data ?? []);
    }


    // ============================================================
    // CREATE UNIVERSITY
    // ============================================================

    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> CreateUniversity(
        string? universityName,
        int? provinceId,
        int? wardId,
        string? addressLine,
        decimal? latitude,
        decimal? longitude,
        bool isActive = true,
        string? returnUrl = null,
        CancellationToken cancellationToken = default)
    {
        var validation =
            ValidateUniversityInput(
                universityName,
                provinceId,
                wardId,
                addressLine,
                latitude,
                longitude);


        if (validation is not null)
        {
            TempData["ErrorMessage"] =
                validation;

            return RedirectBack(
                returnUrl);
        }


        var result =
            await _catalogService
                .CreateUniversityAsync(
                    new SaveUniversityRequestDto
                    {
                        UniversityName =
                            universityName!.Trim(),

                        ProvinceId =
                            provinceId,

                        WardId =
                            wardId,

                        AddressLine =
                            NormalizeOptional(
                                addressLine),

                        Latitude =
                            latitude,

                        Longitude =
                            longitude,

                        IsActive =
                            isActive
                    },
                    cancellationToken);


        if (!result.IsSuccess &&
            result.StatusCode == 401)
        {
            await SignOutAsync();

            return RedirectToAction(
                "Login",
                "Auth");
        }


        if (!result.IsSuccess)
        {
            TempData["ErrorMessage"] =
                result.Message
                ?? "Không thể tạo trường đại học.";

            return RedirectBack(
                returnUrl);
        }


        TempData["SuccessMessage"] =
            "Đã thêm trường đại học mới.";


        return RedirectBack(
            returnUrl);
    }


    // ============================================================
    // UPDATE UNIVERSITY
    // ============================================================

    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> UpdateUniversity(
        long universityId,
        string? universityName,
        int? provinceId,
        int? wardId,
        string? addressLine,
        decimal? latitude,
        decimal? longitude,
        bool isActive,
        string? returnUrl = null,
        CancellationToken cancellationToken = default)
    {
        if (universityId <= 0)
        {
            TempData["ErrorMessage"] =
                "Trường đại học không hợp lệ.";

            return RedirectBack(
                returnUrl);
        }


        var validation =
            ValidateUniversityInput(
                universityName,
                provinceId,
                wardId,
                addressLine,
                latitude,
                longitude);


        if (validation is not null)
        {
            TempData["ErrorMessage"] =
                validation;

            return RedirectBack(
                returnUrl);
        }


        var result =
            await _catalogService
                .UpdateUniversityAsync(
                    universityId,
                    new SaveUniversityRequestDto
                    {
                        UniversityName =
                            universityName!.Trim(),

                        ProvinceId =
                            provinceId,

                        WardId =
                            wardId,

                        AddressLine =
                            NormalizeOptional(
                                addressLine),

                        Latitude =
                            latitude,

                        Longitude =
                            longitude,

                        IsActive =
                            isActive
                    },
                    cancellationToken);


        if (!result.IsSuccess &&
            result.StatusCode == 401)
        {
            await SignOutAsync();

            return RedirectToAction(
                "Login",
                "Auth");
        }


        if (!result.IsSuccess)
        {
            TempData["ErrorMessage"] =
                result.Message
                ?? "Không thể cập nhật trường đại học.";

            return RedirectBack(
                returnUrl);
        }


        TempData["SuccessMessage"] =
            "Đã cập nhật trường đại học.";


        return RedirectBack(
            returnUrl);
    }


    // ============================================================
    // ACTIVATE UNIVERSITY
    // ============================================================

    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> ActivateUniversity(
        long universityId,
        string? returnUrl = null,
        CancellationToken cancellationToken = default)
    {
        if (universityId <= 0)
        {
            TempData["ErrorMessage"] =
                "Trường đại học không hợp lệ.";

            return RedirectBack(
                returnUrl);
        }


        var result =
            await _catalogService
                .ActivateUniversityAsync(
                    universityId,
                    cancellationToken);


        if (!result.IsSuccess &&
            result.StatusCode == 401)
        {
            await SignOutAsync();

            return RedirectToAction(
                "Login",
                "Auth");
        }


        if (!result.IsSuccess)
        {
            TempData["ErrorMessage"] =
                result.Message
                ?? "Không thể kích hoạt trường đại học.";

            return RedirectBack(
                returnUrl);
        }


        TempData["SuccessMessage"] =
            "Trường đại học đã được kích hoạt.";


        return RedirectBack(
            returnUrl);
    }


    // ============================================================
    // DEACTIVATE UNIVERSITY
    // ============================================================

    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> DeactivateUniversity(
        long universityId,
        string? returnUrl = null,
        CancellationToken cancellationToken = default)
    {
        if (universityId <= 0)
        {
            TempData["ErrorMessage"] =
                "Trường đại học không hợp lệ.";

            return RedirectBack(
                returnUrl);
        }


        var result =
            await _catalogService
                .DeactivateUniversityAsync(
                    universityId,
                    cancellationToken);


        if (!result.IsSuccess &&
            result.StatusCode == 401)
        {
            await SignOutAsync();

            return RedirectToAction(
                "Login",
                "Auth");
        }


        if (!result.IsSuccess)
        {
            TempData["ErrorMessage"] =
                result.Message
                ?? "Không thể vô hiệu hóa trường đại học.";

            return RedirectBack(
                returnUrl);
        }


        TempData["SuccessMessage"] =
            "Trường đại học đã được vô hiệu hóa.";


        return RedirectBack(
            returnUrl);
    }


    // ============================================================
    // HELPERS
    // ============================================================

    private static string? ValidateUniversityInput(
        string? universityName,
        int? provinceId,
        int? wardId,
        string? addressLine,
        decimal? latitude,
        decimal? longitude)
    {
        var name =
            universityName?.Trim();


        if (string.IsNullOrWhiteSpace(name))
        {
            return "Vui lòng nhập tên trường đại học.";
        }


        if (name.Length > 255)
        {
            return "Tên trường đại học không được vượt quá 255 ký tự.";
        }


        if (wardId.HasValue &&
            !provinceId.HasValue)
        {
            return "Phải chọn tỉnh/thành phố trước khi chọn phường/xã.";
        }


        if (!string.IsNullOrWhiteSpace(addressLine) &&
            addressLine.Trim().Length > 500)
        {
            return "Địa chỉ không được vượt quá 500 ký tự.";
        }


        if (latitude.HasValue &&
            (latitude.Value < -90 ||
             latitude.Value > 90))
        {
            return "Vĩ độ phải nằm trong khoảng từ -90 đến 90.";
        }


        if (longitude.HasValue &&
            (longitude.Value < -180 ||
             longitude.Value > 180))
        {
            return "Kinh độ phải nằm trong khoảng từ -180 đến 180.";
        }


        return null;
    }


    private static string? NormalizeOptional(
        string? value)
    {
        return string.IsNullOrWhiteSpace(value)
            ? null
            : value.Trim();
    }


    private IActionResult RedirectBack(
        string? returnUrl)
    {
        if (!string.IsNullOrWhiteSpace(
                returnUrl)
            &&
            Url.IsLocalUrl(
                returnUrl))
        {
            return LocalRedirect(
                returnUrl);
        }


        return RedirectToAction(
            nameof(Index));
    }


    private async Task SignOutAsync()
    {
        HttpContext.Session.Clear();

        await HttpContext.SignOutAsync(
            CookieAuthenticationDefaults
                .AuthenticationScheme);
    }
}