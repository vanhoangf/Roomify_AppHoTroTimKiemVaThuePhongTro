using Microsoft.EntityFrameworkCore;
using Roomify.API.Data;
using Roomify.API.DTOs.Admin.Users;
using Roomify.API.Entities;
using Roomify.API.Exceptions;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Services.Implementations;

public class AdminUserService : IAdminUserService
{
    private readonly RoomifyDbContext _db;

    private static readonly HashSet<string> ValidRoles =
        new(StringComparer.OrdinalIgnoreCase)
        {
            "STUDENT",
            "LANDLORD",
            "ADMIN"
        };

    private static readonly HashSet<string> ValidStatuses =
        new(StringComparer.OrdinalIgnoreCase)
        {
            "ACTIVE",
            "LOCKED"
        };

    public AdminUserService(RoomifyDbContext db)
    {
        _db = db;
    }

    // ============================================================
    // GET USERS
    // ============================================================

    public async Task<AdminUserListResponse> GetUsersAsync(
        string? role,
        string? status,
        bool? verified,
        string? search,
        int page,
        int pageSize)
    {
        ValidatePaging(page, pageSize);

        var normalizedRole = ValidateRoleFilter(role);
        var normalizedStatus = ValidateStatusFilter(status);
        var normalizedSearch = NormalizeOptional(search);

        var query = _db.Users
            .AsNoTracking()
            .AsQueryable();

        if (normalizedRole is not null)
        {
            query = query.Where(x =>
                x.Role.RoleCode == normalizedRole);
        }

        if (normalizedStatus is not null)
        {
            query = query.Where(x =>
                x.Status == normalizedStatus);
        }

        if (verified.HasValue)
        {
            query = query.Where(x =>
                (
                    x.StudentProfileUser != null &&
                    x.StudentProfileUser.IsVerified == verified.Value
                )
                ||
                (
                    x.LandlordProfileUser != null &&
                    x.LandlordProfileUser.IsVerified == verified.Value
                ));
        }

        if (normalizedSearch is not null)
        {
            query = query.Where(x =>
                x.FullName.Contains(normalizedSearch) ||
                x.Email.Contains(normalizedSearch) ||
                (x.Phone != null &&
                 x.Phone.Contains(normalizedSearch)));
        }

        var totalCount = await query.CountAsync();

        var items = await ProjectResponse(query)
            .OrderByDescending(x => x.CreatedAt)
            .ThenByDescending(x => x.UserId)
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .ToListAsync();

        return BuildListResponse(
            items,
            totalCount,
            page,
            pageSize);
    }

    // ============================================================
    // GET USER DETAIL
    // ============================================================

    public async Task<AdminUserResponse> GetUserByIdAsync(
        long userId)
    {
        var result = await ProjectResponse(
                _db.Users
                    .AsNoTracking()
                    .Where(x => x.UserId == userId))
            .FirstOrDefaultAsync();

        if (result is null)
        {
            throw new NotFoundException(
                "USER_NOT_FOUND",
                "Không tìm thấy người dùng.");
        }

        return result;
    }

    // ============================================================
    // LOCK USER
    // ============================================================

    public async Task<AdminUserResponse> LockUserAsync(
        long adminUserId,
        long userId,
        LockUserRequest request)
    {
        if (adminUserId == userId)
        {
            throw new ConflictException(
                "CANNOT_LOCK_SELF",
                "Quản trị viên không thể tự khóa tài khoản của mình.");
        }

        var user = await _db.Users
            .FirstOrDefaultAsync(x =>
                x.UserId == userId);

        if (user is null)
        {
            throw new NotFoundException(
                "USER_NOT_FOUND",
                "Không tìm thấy người dùng.");
        }

        if (string.Equals(
                user.Status,
                "LOCKED",
                StringComparison.OrdinalIgnoreCase))
        {
            throw new ConflictException(
                "USER_ALREADY_LOCKED",
                "Tài khoản đã bị khóa.");
        }

        var reason = NormalizeOptional(request.Reason);

        if (reason is null)
        {
            throw new BadRequestException(
                "LOCK_REASON_REQUIRED",
                "Vui lòng nhập lý do khóa tài khoản.");
        }

        var now = DateTime.UtcNow;

        user.Status = "LOCKED";
        user.LockedReason = reason;
        user.LockedAt = now;
        user.LockedByUserId = adminUserId;
        user.UpdatedAt = now;

        await _db.SaveChangesAsync();

        return await GetUserByIdAsync(userId);
    }

    // ============================================================
    // UNLOCK USER
    // ============================================================

    public async Task<AdminUserResponse> UnlockUserAsync(
        long adminUserId,
        long userId)
    {
        if (adminUserId == userId)
        {
            throw new ConflictException(
                "CANNOT_UNLOCK_SELF",
                "Tài khoản quản trị viên hiện tại không cần thao tác mở khóa.");
        }

        var user = await _db.Users
            .FirstOrDefaultAsync(x =>
                x.UserId == userId);

        if (user is null)
        {
            throw new NotFoundException(
                "USER_NOT_FOUND",
                "Không tìm thấy người dùng.");
        }

        if (!string.Equals(
                user.Status,
                "LOCKED",
                StringComparison.OrdinalIgnoreCase))
        {
            throw new ConflictException(
                "USER_NOT_LOCKED",
                "Tài khoản hiện không bị khóa.");
        }

        var now = DateTime.UtcNow;

        user.Status = "ACTIVE";
        user.LockedReason = null;
        user.LockedAt = null;
        user.LockedByUserId = null;
        user.UpdatedAt = now;

        await _db.SaveChangesAsync();

        return await GetUserByIdAsync(userId);
    }

    // ============================================================
    // VERIFY STUDENT
    // ============================================================

    public async Task<AdminUserResponse> VerifyStudentAsync(
        long adminUserId,
        long studentId)
    {
        var profile = await _db.StudentProfiles
            .Include(x => x.User)
            .FirstOrDefaultAsync(x =>
                x.StudentId == studentId);

        if (profile is null)
        {
            throw new NotFoundException(
                "STUDENT_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ sinh viên.");
        }

        if (profile.IsVerified)
        {
            throw new ConflictException(
                "STUDENT_ALREADY_VERIFIED",
                "Hồ sơ sinh viên đã được xác minh.");
        }

        if (profile.UniversityId is null ||
            string.IsNullOrWhiteSpace(profile.StudentCode) ||
            string.IsNullOrWhiteSpace(profile.StudentCardUrl))
        {
            throw new ConflictException(
                "STUDENT_PROFILE_INCOMPLETE",
                "Hồ sơ sinh viên chưa đầy đủ thông tin để xác minh.");
        }

        if (!string.Equals(
                profile.User.Status,
                "ACTIVE",
                StringComparison.OrdinalIgnoreCase))
        {
            throw new ConflictException(
                "USER_ACCOUNT_LOCKED",
                "Không thể xác minh hồ sơ của tài khoản đang bị khóa.");
        }

        var now = DateTime.UtcNow;

        profile.IsVerified = true;
        profile.VerifiedByUserId = adminUserId;
        profile.VerifiedAt = now;
        profile.UpdatedAt = now;

        await _db.SaveChangesAsync();

        return await GetUserByIdAsync(profile.UserId);
    }

    // ============================================================
    // VERIFY LANDLORD
    // ============================================================

    public async Task<AdminUserResponse> VerifyLandlordAsync(
        long adminUserId,
        long landlordId)
    {
        var profile = await _db.LandlordProfiles
            .Include(x => x.User)
            .FirstOrDefaultAsync(x =>
                x.LandlordId == landlordId);

        if (profile is null)
        {
            throw new NotFoundException(
                "LANDLORD_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ chủ trọ.");
        }

        if (profile.IsVerified)
        {
            throw new ConflictException(
                "LANDLORD_ALREADY_VERIFIED",
                "Hồ sơ chủ trọ đã được xác minh.");
        }

        if (string.IsNullOrWhiteSpace(profile.IdentityNumber) ||
            string.IsNullOrWhiteSpace(profile.IdentityDocumentUrl))
        {
            throw new ConflictException(
                "LANDLORD_PROFILE_INCOMPLETE",
                "Hồ sơ chủ trọ chưa đầy đủ thông tin để xác minh.");
        }

        if (!string.Equals(
                profile.User.Status,
                "ACTIVE",
                StringComparison.OrdinalIgnoreCase))
        {
            throw new ConflictException(
                "USER_ACCOUNT_LOCKED",
                "Không thể xác minh hồ sơ của tài khoản đang bị khóa.");
        }

        var now = DateTime.UtcNow;

        profile.IsVerified = true;
        profile.VerifiedByUserId = adminUserId;
        profile.VerifiedAt = now;
        profile.UpdatedAt = now;

        await _db.SaveChangesAsync();

        return await GetUserByIdAsync(profile.UserId);
    }

    // ============================================================
    // PROJECTION
    // ============================================================

    private static IQueryable<AdminUserResponse> ProjectResponse(
        IQueryable<User> query)
    {
        return query.Select(x => new AdminUserResponse
        {
            UserId = x.UserId,

            RoleId = x.RoleId,
            RoleCode = x.Role.RoleCode,
            RoleName = x.Role.RoleName,

            Email = x.Email,
            FullName = x.FullName,
            Phone = x.Phone,
            AvatarUrl = x.AvatarUrl,

            Status = x.Status,
            EmailVerified = x.EmailVerified,

            LockedReason = x.LockedReason,
            LockedAt = x.LockedAt,
            LockedByUserId = x.LockedByUserId,

            LockedByUserName =
                x.LockedByUser != null
                    ? x.LockedByUser.FullName
                    : null,

            LastLoginAt = x.LastLoginAt,

            StudentId =
                x.StudentProfileUser != null
                    ? x.StudentProfileUser.StudentId
                    : null,

            LandlordId =
                x.LandlordProfileUser != null
                    ? x.LandlordProfileUser.LandlordId
                    : null,

            IsProfileVerified =
                x.StudentProfileUser != null
                    ? x.StudentProfileUser.IsVerified
                    : x.LandlordProfileUser != null
                        ? x.LandlordProfileUser.IsVerified
                        : null,

            VerifiedByUserId =
                x.StudentProfileUser != null
                    ? x.StudentProfileUser.VerifiedByUserId
                    : x.LandlordProfileUser != null
                        ? x.LandlordProfileUser.VerifiedByUserId
                        : null,

            VerifiedByUserName =
                x.StudentProfileUser != null &&
                x.StudentProfileUser.VerifiedByUser != null
                    ? x.StudentProfileUser.VerifiedByUser.FullName
                    : x.LandlordProfileUser != null &&
                      x.LandlordProfileUser.VerifiedByUser != null
                        ? x.LandlordProfileUser.VerifiedByUser.FullName
                        : null,

            VerifiedAt =
                x.StudentProfileUser != null
                    ? x.StudentProfileUser.VerifiedAt
                    : x.LandlordProfileUser != null
                        ? x.LandlordProfileUser.VerifiedAt
                        : null,

            UniversityId =
                x.StudentProfileUser != null
                    ? x.StudentProfileUser.UniversityId
                    : null,

            UniversityName =
                x.StudentProfileUser != null &&
                x.StudentProfileUser.University != null
                    ? x.StudentProfileUser.University.UniversityName
                    : null,

            StudentCode =
                x.StudentProfileUser != null
                    ? x.StudentProfileUser.StudentCode
                    : null,

            StudentCardUrl =
                x.StudentProfileUser != null
                    ? x.StudentProfileUser.StudentCardUrl
                    : null,

            IdentityNumber =
                x.LandlordProfileUser != null
                    ? x.LandlordProfileUser.IdentityNumber
                    : null,

            IdentityDocumentUrl =
                x.LandlordProfileUser != null
                    ? x.LandlordProfileUser.IdentityDocumentUrl
                    : null,

            CreatedAt = x.CreatedAt,
            UpdatedAt = x.UpdatedAt
        });
    }

    // ============================================================
    // FILTER VALIDATION
    // ============================================================

    private static string? ValidateRoleFilter(
        string? role)
    {
        var value = NormalizeOptional(role);

        if (value is null)
        {
            return null;
        }

        if (!ValidRoles.Contains(value))
        {
            throw new BadRequestException(
                "INVALID_ROLE",
                "Role không hợp lệ.");
        }

        return value.ToUpperInvariant();
    }

    private static string? ValidateStatusFilter(
        string? status)
    {
        var value = NormalizeOptional(status);

        if (value is null)
        {
            return null;
        }

        if (!ValidStatuses.Contains(value))
        {
            throw new BadRequestException(
                "INVALID_USER_STATUS",
                "Trạng thái tài khoản không hợp lệ.");
        }

        return value.ToUpperInvariant();
    }

    private static void ValidatePaging(
        int page,
        int pageSize)
    {
        if (page < 1 || page > 100000)
        {
            throw new BadRequestException(
                "INVALID_PAGE",
                "Page phải nằm trong khoảng từ 1 đến 100000.");
        }

        if (pageSize < 1 || pageSize > 50)
        {
            throw new BadRequestException(
                "INVALID_PAGE_SIZE",
                "PageSize phải nằm trong khoảng từ 1 đến 50.");
        }
    }

    // ============================================================
    // LIST RESPONSE
    // ============================================================

    private static AdminUserListResponse BuildListResponse(
        List<AdminUserResponse> items,
        int totalCount,
        int page,
        int pageSize)
    {
        var totalPages = totalCount == 0
            ? 0
            : (int)Math.Ceiling(
                totalCount / (double)pageSize);

        return new AdminUserListResponse
        {
            Items = items,
            TotalCount = totalCount,
            Page = page,
            PageSize = pageSize,
            TotalPages = totalPages,
            HasNextPage = page < totalPages,
            HasPreviousPage = page > 1
        };
    }

    // ============================================================
    // NORMALIZATION
    // ============================================================

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