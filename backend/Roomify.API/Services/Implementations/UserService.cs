using Microsoft.EntityFrameworkCore;

using Roomify.API.Data;
using Roomify.API.DTOs.Users;
using Roomify.API.Exceptions;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Services.Implementations;

public class UserService : IUserService
{
    private readonly RoomifyDbContext _dbContext;

    public UserService(
        RoomifyDbContext dbContext)
    {
        _dbContext = dbContext;
    }

    // ======================================================
    // GET CURRENT USER
    // ======================================================

    public async Task<UserMeResponse> GetMeAsync(
        long userId,
        CancellationToken cancellationToken = default)
    {
        var user =
            await _dbContext.Users
                .AsNoTracking()
                .Include(x => x.Role)
                .SingleOrDefaultAsync(
                    x => x.UserId == userId,
                    cancellationToken);

        if (user is null)
        {
            throw new NotFoundException(
                "USER_NOT_FOUND",
                "Không tìm thấy tài khoản.");
        }

        var response =
            new UserMeResponse
            {
                UserId =
                    user.UserId,

                Email =
                    user.Email,

                FullName =
                    user.FullName,

                Phone =
                    user.Phone,

                AvatarUrl =
                    user.AvatarUrl,

                Role =
                    user.Role.RoleCode,

                Status =
                    user.Status,

                EmailVerified =
                    user.EmailVerified,

                LastLoginAt =
                    user.LastLoginAt,

                CreatedAt =
                    user.CreatedAt,

                UpdatedAt =
                    user.UpdatedAt
            };

        // ==============================================
        // STUDENT
        // ==============================================

        if (user.Role.RoleCode == "STUDENT")
        {
            response.StudentProfile =
                await GetStudentProfileAsync(
                    userId,
                    cancellationToken);
        }

        // ==============================================
        // LANDLORD
        // ==============================================

        if (user.Role.RoleCode == "LANDLORD")
        {
            response.LandlordProfile =
                await GetLandlordProfileAsync(
                    userId,
                    cancellationToken);
        }

        return response;
    }

    // ======================================================
    // UPDATE COMMON USER PROFILE
    // ======================================================

    public async Task<UserMeResponse> UpdateMeAsync(
        long userId,
        UpdateUserRequest request,
        CancellationToken cancellationToken = default)
    {
        var user =
            await _dbContext.Users
                .SingleOrDefaultAsync(
                    x => x.UserId == userId,
                    cancellationToken);

        if (user is null)
        {
            throw new NotFoundException(
                "USER_NOT_FOUND",
                "Không tìm thấy tài khoản.");
        }

        var fullName =
            request.FullName.Trim();

        var phone =
            NormalizeNullable(
                request.Phone);

        // Phone phải unique nếu có giá trị
        if (phone is not null)
        {
            var phoneExists =
                await _dbContext.Users
                    .AnyAsync(
                        x =>
                            x.Phone == phone &&
                            x.UserId != userId,
                        cancellationToken);

            if (phoneExists)
            {
                throw new ConflictException(
                    "PHONE_ALREADY_EXISTS",
                    "Số điện thoại đã được sử dụng.");
            }
        }

        user.FullName =
            fullName;

        user.Phone =
            phone;

        user.UpdatedAt =
            DateTime.UtcNow;

        await _dbContext.SaveChangesAsync(
            cancellationToken);

        return await GetMeAsync(
            userId,
            cancellationToken);
    }

    // ======================================================
    // UPDATE STUDENT PROFILE
    // ======================================================

    public async Task<StudentProfileResponse>
        UpdateStudentProfileAsync(
            long userId,
            UpdateStudentProfileRequest request,
            CancellationToken cancellationToken = default)
    {
        var profile =
            await _dbContext.StudentProfiles
                .SingleOrDefaultAsync(
                    x => x.UserId == userId,
                    cancellationToken);

        if (profile is null)
        {
            throw new NotFoundException(
                "STUDENT_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ sinh viên.");
        }

        var studentCode =
            NormalizeNullable(
                request.StudentCode);

        // ==============================================
        // UNIVERSITY VALIDATION
        // ==============================================

        if (request.UniversityId.HasValue)
        {
            var universityExists =
                await _dbContext.Universities
                    .AnyAsync(
                        x =>
                            x.UniversityId ==
                                request.UniversityId.Value
                            &&
                            x.IsActive,
                        cancellationToken);

            if (!universityExists)
            {
                throw new BadRequestException(
                    "UNIVERSITY_NOT_FOUND",
                    "Trường đại học không tồn tại hoặc đã ngừng hoạt động.");
            }
        }

        // ==============================================
        // STUDENT CODE UNIQUE PER UNIVERSITY
        // ==============================================

        if (request.UniversityId.HasValue &&
            studentCode is not null)
        {
            var studentCodeExists =
                await _dbContext.StudentProfiles
                    .AnyAsync(
                        x =>
                            x.StudentId !=
                                profile.StudentId
                            &&
                            x.UniversityId ==
                                request.UniversityId.Value
                            &&
                            x.StudentCode ==
                                studentCode,
                        cancellationToken);

            if (studentCodeExists)
            {
                throw new ConflictException(
                    "STUDENT_CODE_ALREADY_EXISTS",
                    "Mã sinh viên đã tồn tại tại trường đại học này.");
            }
        }

        // Nếu thông tin dùng để xác minh thay đổi
        // thì kết quả xác minh cũ không còn đáng tin cậy.
        var verificationDataChanged =
            profile.UniversityId !=
                request.UniversityId
            ||
            profile.StudentCode !=
                studentCode;

        profile.UniversityId =
            request.UniversityId;

        profile.StudentCode =
            studentCode;

        profile.UpdatedAt =
            DateTime.UtcNow;

        if (verificationDataChanged)
        {
            profile.IsVerified =
                false;

            profile.VerifiedByUserId =
                null;

            profile.VerifiedAt =
                null;
        }

        await _dbContext.SaveChangesAsync(
            cancellationToken);

        return await GetStudentProfileAsync(
            userId,
            cancellationToken);
    }

    // ======================================================
    // UPDATE LANDLORD PROFILE
    // ======================================================

    public async Task<LandlordProfileResponse>
        UpdateLandlordProfileAsync(
            long userId,
            UpdateLandlordProfileRequest request,
            CancellationToken cancellationToken = default)
    {
        var profile =
            await _dbContext.LandlordProfiles
                .SingleOrDefaultAsync(
                    x => x.UserId == userId,
                    cancellationToken);

        if (profile is null)
        {
            throw new NotFoundException(
                "LANDLORD_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ chủ trọ.");
        }

        var identityNumber =
            NormalizeNullable(
                request.IdentityNumber);

        if (identityNumber is not null)
        {
            var identityExists =
                await _dbContext.LandlordProfiles
                    .AnyAsync(
                        x =>
                            x.IdentityNumber ==
                                identityNumber
                            &&
                            x.LandlordId !=
                                profile.LandlordId,
                        cancellationToken);

            if (identityExists)
            {
                throw new ConflictException(
                    "IDENTITY_NUMBER_ALREADY_EXISTS",
                    "Số CCCD/giấy tờ đã được sử dụng.");
            }
        }

        var verificationDataChanged =
            profile.IdentityNumber !=
                identityNumber;

        profile.IdentityNumber =
            identityNumber;

        profile.UpdatedAt =
            DateTime.UtcNow;

        if (verificationDataChanged)
        {
            profile.IsVerified =
                false;

            profile.VerifiedByUserId =
                null;

            profile.VerifiedAt =
                null;
        }

        await _dbContext.SaveChangesAsync(
            cancellationToken);

        return await GetLandlordProfileAsync(
            userId,
            cancellationToken);
    }

    // ======================================================
    // GET STUDENT PROFILE
    // ======================================================

    private async Task<StudentProfileResponse>
        GetStudentProfileAsync(
            long userId,
            CancellationToken cancellationToken)
    {
        var profile =
            await _dbContext.StudentProfiles
                .AsNoTracking()
                .SingleOrDefaultAsync(
                    x => x.UserId == userId,
                    cancellationToken);

        if (profile is null)
        {
            throw new NotFoundException(
                "STUDENT_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ sinh viên.");
        }

        string? universityName =
            null;

        if (profile.UniversityId.HasValue)
        {
            universityName =
                await _dbContext.Universities
                    .AsNoTracking()
                    .Where(
                        x =>
                            x.UniversityId ==
                            profile.UniversityId.Value)
                    .Select(
                        x =>
                            x.UniversityName)
                    .SingleOrDefaultAsync(
                        cancellationToken);
        }

        return new StudentProfileResponse
        {
            StudentId =
                profile.StudentId,

            UniversityId =
                profile.UniversityId,

            UniversityName =
                universityName,

            StudentCode =
                profile.StudentCode,

            StudentCardUrl =
                profile.StudentCardUrl,

            IsVerified =
                profile.IsVerified,

            VerifiedAt =
                profile.VerifiedAt
        };
    }

    // ======================================================
    // GET LANDLORD PROFILE
    // ======================================================

    private async Task<LandlordProfileResponse>
        GetLandlordProfileAsync(
            long userId,
            CancellationToken cancellationToken)
    {
        var profile =
            await _dbContext.LandlordProfiles
                .AsNoTracking()
                .SingleOrDefaultAsync(
                    x => x.UserId == userId,
                    cancellationToken);

        if (profile is null)
        {
            throw new NotFoundException(
                "LANDLORD_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ chủ trọ.");
        }

        return new LandlordProfileResponse
        {
            LandlordId =
                profile.LandlordId,

            IdentityNumber =
                profile.IdentityNumber,

            IdentityDocumentUrl =
                profile.IdentityDocumentUrl,

            IsVerified =
                profile.IsVerified,

            VerifiedAt =
                profile.VerifiedAt
        };
    }

    // ======================================================
    // NORMALIZE NULLABLE
    // ======================================================

    private static string? NormalizeNullable(
        string? value)
    {
        if (string.IsNullOrWhiteSpace(value))
        {
            return null;
        }

        return value.Trim();
    }
}