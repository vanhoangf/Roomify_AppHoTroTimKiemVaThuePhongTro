using Microsoft.AspNetCore.Identity;
using Microsoft.Data.SqlClient;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Options;

using Roomify.API.Data;
using Roomify.API.DTOs.Auth;
using Roomify.API.Entities;
using Roomify.API.Exceptions;
using Roomify.API.Helpers;
using Roomify.API.Options;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Services.Implementations;

public class AuthService : IAuthService
{
    private readonly RoomifyDbContext _dbContext;
    private readonly IPasswordHasher<User> _passwordHasher;
    private readonly IJwtTokenService _jwtTokenService;
    private readonly JwtOptions _jwtOptions;

    public AuthService(
        RoomifyDbContext dbContext,
        IPasswordHasher<User> passwordHasher,
        IJwtTokenService jwtTokenService,
        IOptions<JwtOptions> jwtOptions)
    {
        _dbContext = dbContext;
        _passwordHasher = passwordHasher;
        _jwtTokenService = jwtTokenService;
        _jwtOptions = jwtOptions.Value;
    }

    // ======================================================
    // REGISTER STUDENT
    // ======================================================

    public async Task<AuthUserResponse> RegisterStudentAsync(
        RegisterStudentRequest request,
        CancellationToken cancellationToken = default)
    {
        var email =
            request.Email.Trim().ToLowerInvariant();

        var fullName =
            request.FullName.Trim();

        var phone =
            NormalizeNullable(request.Phone);

        var studentCode =
            NormalizeNullable(request.StudentCode);

        // Kiểm tra Email / Phone
        await ValidateCommonUserDataAsync(
            email,
            phone,
            cancellationToken);

        // Kiểm tra trường đại học
        if (request.UniversityId.HasValue)
        {
            var universityExists =
                await _dbContext.Universities
                    .AnyAsync(
                        x =>
                            x.UniversityId ==
                            request.UniversityId.Value,
                        cancellationToken);

            if (!universityExists)
            {
                throw new BadRequestException(
                    "UNIVERSITY_NOT_FOUND",
                    "Trường đại học không tồn tại.");
            }
        }

        // Kiểm tra mã sinh viên trong cùng trường
        if (request.UniversityId.HasValue &&
            studentCode is not null)
        {
            var studentCodeExists =
                await _dbContext.StudentProfiles
                    .AnyAsync(
                        x =>
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

        // Lấy Role STUDENT
        var role =
            await GetRoleAsync(
                "STUDENT",
                cancellationToken);

        // Users + StudentProfiles phải cùng thành công
        await using var transaction =
            await _dbContext.Database
                .BeginTransactionAsync(
                    cancellationToken);

        try
        {
            var now =
                DateTime.UtcNow;

            var user =
                new User
                {
                    RoleId = role.RoleId,

                    Email = email,

                    PasswordHash =
                        string.Empty,

                    FullName =
                        fullName,

                    Phone =
                        phone,

                    Status =
                        "ACTIVE",

                    EmailVerified =
                        false,

                    CreatedAt =
                        now,

                    UpdatedAt =
                        now
                };

            // Hash password
            user.PasswordHash =
                _passwordHasher.HashPassword(
                    user,
                    request.Password);

            _dbContext.Users.Add(user);

            // Save để nhận UserID
            await _dbContext.SaveChangesAsync(
                cancellationToken);

            var studentProfile =
                new StudentProfile
                {
                    UserId =
                        user.UserId,

                    UniversityId =
                        request.UniversityId,

                    StudentCode =
                        studentCode,

                    IsVerified =
                        false,

                    CreatedAt =
                        now,

                    UpdatedAt =
                        now
                };

            _dbContext.StudentProfiles.Add(
                studentProfile);

            await _dbContext.SaveChangesAsync(
                cancellationToken);

            await transaction.CommitAsync(
                cancellationToken);

            return MapUserResponse(
                user,
                role.RoleCode);
        }
        catch (DbUpdateException ex)
            when (IsUniqueConstraintViolation(ex))
        {
            await transaction.RollbackAsync(
                cancellationToken);

            throw new ConflictException(
                "DUPLICATE_DATA",
                "Thông tin đăng ký đã tồn tại trong hệ thống.");
        }
        catch
        {
            await transaction.RollbackAsync(
                cancellationToken);

            throw;
        }
    }

    // ======================================================
    // REGISTER LANDLORD
    // ======================================================

    public async Task<AuthUserResponse> RegisterLandlordAsync(
        RegisterLandlordRequest request,
        CancellationToken cancellationToken = default)
    {
        var email =
            request.Email.Trim().ToLowerInvariant();

        var fullName =
            request.FullName.Trim();

        var phone =
            NormalizeNullable(request.Phone);

        var identityNumber =
            NormalizeNullable(
                request.IdentityNumber);

        // Kiểm tra Email / Phone
        await ValidateCommonUserDataAsync(
            email,
            phone,
            cancellationToken);

        // Kiểm tra CCCD / giấy tờ
        if (identityNumber is not null)
        {
            var identityExists =
                await _dbContext.LandlordProfiles
                    .AnyAsync(
                        x =>
                            x.IdentityNumber ==
                            identityNumber,
                        cancellationToken);

            if (identityExists)
            {
                throw new ConflictException(
                    "IDENTITY_NUMBER_ALREADY_EXISTS",
                    "Số CCCD/giấy tờ đã được sử dụng.");
            }
        }

        // Lấy role LANDLORD
        var role =
            await GetRoleAsync(
                "LANDLORD",
                cancellationToken);

        // Users + LandlordProfiles phải cùng thành công
        await using var transaction =
            await _dbContext.Database
                .BeginTransactionAsync(
                    cancellationToken);

        try
        {
            var now =
                DateTime.UtcNow;

            var user =
                new User
                {
                    RoleId =
                        role.RoleId,

                    Email =
                        email,

                    PasswordHash =
                        string.Empty,

                    FullName =
                        fullName,

                    Phone =
                        phone,

                    Status =
                        "ACTIVE",

                    EmailVerified =
                        false,

                    CreatedAt =
                        now,

                    UpdatedAt =
                        now
                };

            // Hash password
            user.PasswordHash =
                _passwordHasher.HashPassword(
                    user,
                    request.Password);

            _dbContext.Users.Add(user);

            // Save để nhận UserID
            await _dbContext.SaveChangesAsync(
                cancellationToken);

            var landlordProfile =
                new LandlordProfile
                {
                    UserId =
                        user.UserId,

                    IdentityNumber =
                        identityNumber,

                    IsVerified =
                        false,

                    CreatedAt =
                        now,

                    UpdatedAt =
                        now
                };

            _dbContext.LandlordProfiles.Add(
                landlordProfile);

            await _dbContext.SaveChangesAsync(
                cancellationToken);

            await transaction.CommitAsync(
                cancellationToken);

            return MapUserResponse(
                user,
                role.RoleCode);
        }
        catch (DbUpdateException ex)
            when (IsUniqueConstraintViolation(ex))
        {
            await transaction.RollbackAsync(
                cancellationToken);

            throw new ConflictException(
                "DUPLICATE_DATA",
                "Thông tin đăng ký đã tồn tại trong hệ thống.");
        }
        catch
        {
            await transaction.RollbackAsync(
                cancellationToken);

            throw;
        }
    }

    // ======================================================
    // LOGIN
    // ======================================================

    public async Task<LoginResponse> LoginAsync(
        LoginRequest request,
        CancellationToken cancellationToken = default)
    {
        var email =
            request.Email
                .Trim()
                .ToLowerInvariant();

        // Lấy User + Role
        var user =
            await _dbContext.Users
                .Include(x => x.Role)
                .SingleOrDefaultAsync(
                    x => x.Email == email,
                    cancellationToken);

        // Không tiết lộ Email có tồn tại hay không
        if (user is null)
        {
            throw new UnauthorizedException(
                "INVALID_CREDENTIALS",
                "Email hoặc mật khẩu không chính xác.");
        }

        // Verify password hash
        var passwordResult =
            _passwordHasher
                .VerifyHashedPassword(
                    user,
                    user.PasswordHash,
                    request.Password);

        if (passwordResult ==
            PasswordVerificationResult.Failed)
        {
            throw new UnauthorizedException(
                "INVALID_CREDENTIALS",
                "Email hoặc mật khẩu không chính xác.");
        }

        // Password đúng rồi mới trả trạng thái LOCKED
        if (user.Status == "LOCKED")
        {
            throw new ForbiddenException(
                "ACCOUNT_LOCKED",
                "Tài khoản đã bị khóa.");
        }

        if (user.Status != "ACTIVE")
        {
            throw new ForbiddenException(
                "ACCOUNT_NOT_ACTIVE",
                "Tài khoản hiện không hoạt động.");
        }

        if (user.Role is null)
        {
            throw new InvalidOperationException(
                "Tài khoản chưa được gán vai trò.");
        }

        // Nếu thuật toán hash cũ thì hash lại
        if (passwordResult ==
            PasswordVerificationResult
                .SuccessRehashNeeded)
        {
            user.PasswordHash =
                _passwordHasher.HashPassword(
                    user,
                    request.Password);
        }

        var now =
            DateTime.UtcNow;

        // Cập nhật LastLoginAt
        user.LastLoginAt =
            now;

        user.UpdatedAt =
            now;

        // Sinh Access Token
        var (
            accessToken,
            accessTokenExpiresAt
        ) =
            _jwtTokenService
                .GenerateAccessToken(
                    user,
                    user.Role.RoleCode);

        // Sinh và lưu Refresh Token
        var (
            refreshToken,
            refreshTokenExpiresAt
        ) =
            await CreateRefreshTokenAsync(
                user,
                cancellationToken);

        // CreateRefreshTokenAsync có SaveChanges,
        // nên LastLoginAt và PasswordHash mới cũng
        // được lưu cùng lúc.

        return new LoginResponse
        {
            AccessToken =
                accessToken,

            RefreshToken =
                refreshToken,

            TokenType =
                "Bearer",

            AccessTokenExpiresAt =
                accessTokenExpiresAt,

            RefreshTokenExpiresAt =
                refreshTokenExpiresAt,

            User =
                new AuthUserResponse
                {
                    UserId =
                        user.UserId,

                    Email =
                        user.Email,

                    FullName =
                        user.FullName,

                    Role =
                        user.Role.RoleCode,

                    Status =
                        user.Status,

                    CreatedAt =
                        user.CreatedAt
                }
        };
    }

    // ======================================================
    // REFRESH TOKEN
    // ======================================================

    public async Task<LoginResponse> RefreshTokenAsync(
        RefreshTokenRequest request,
        CancellationToken cancellationToken = default)
    {
        var now =
            DateTime.UtcNow;

        // Client gửi raw token.
        // DB chỉ lưu hash.
        var tokenHash =
            RefreshTokenHelper.HashToken(
                request.RefreshToken);

        // Tìm Refresh Token và User + Role
        var storedToken =
            await _dbContext.RefreshTokens
                .Include(x => x.User)
                    .ThenInclude(x => x.Role)
                .SingleOrDefaultAsync(
                    x =>
                        x.TokenHash ==
                        tokenHash,
                    cancellationToken);

        // Token không tồn tại
        if (storedToken is null)
        {
            throw new UnauthorizedException(
                "INVALID_REFRESH_TOKEN",
                "Refresh token không hợp lệ.");
        }

        // Token đã bị revoke
        if (storedToken.RevokedAt.HasValue)
        {
            throw new UnauthorizedException(
                "REFRESH_TOKEN_REVOKED",
                "Refresh token đã bị thu hồi.");
        }

        // Token hết hạn
        if (storedToken.ExpiresAt <= now)
        {
            throw new UnauthorizedException(
                "REFRESH_TOKEN_EXPIRED",
                "Refresh token đã hết hạn.");
        }

        var user =
            storedToken.User;

        if (user is null)
        {
            throw new UnauthorizedException(
                "INVALID_REFRESH_TOKEN",
                "Refresh token không hợp lệ.");
        }

        // Kiểm tra tài khoản
        if (user.Status == "LOCKED")
        {
            throw new ForbiddenException(
                "ACCOUNT_LOCKED",
                "Tài khoản đã bị khóa.");
        }

        if (user.Status != "ACTIVE")
        {
            throw new ForbiddenException(
                "ACCOUNT_NOT_ACTIVE",
                "Tài khoản hiện không hoạt động.");
        }

        if (user.Role is null)
        {
            throw new InvalidOperationException(
                "Tài khoản chưa được gán vai trò.");
        }

        // ==================================================
        // TOKEN ROTATION
        //
        // Refresh Token cũ bị revoke.
        // Refresh Token mới được sinh ra.
        //
        // Hai thao tác phải cùng transaction.
        // ==================================================

        await using var transaction =
            await _dbContext.Database
                .BeginTransactionAsync(
                    cancellationToken);

        try
        {
            // Revoke Refresh Token cũ
            storedToken.RevokedAt =
                now;

            // Sinh raw Refresh Token mới
            var newRawRefreshToken =
                RefreshTokenHelper
                    .GenerateToken();

            // Hash trước khi lưu DB
            var newRefreshTokenHash =
                RefreshTokenHelper
                    .HashToken(
                        newRawRefreshToken);

            var newRefreshTokenExpiresAt =
                now.AddDays(
                    _jwtOptions
                        .RefreshTokenDays);

            // Tạo Refresh Token mới
            var newRefreshToken =
                new RefreshToken
                {
                    UserId =
                        user.UserId,

                    TokenHash =
                        newRefreshTokenHash,

                    ExpiresAt =
                        newRefreshTokenExpiresAt,

                    RevokedAt =
                        null,

                    CreatedAt =
                        now
                };

            _dbContext.RefreshTokens.Add(
                newRefreshToken);

            // Sinh Access Token mới
            var (
                accessToken,
                accessTokenExpiresAt
            ) =
                _jwtTokenService
                    .GenerateAccessToken(
                        user,
                        user.Role.RoleCode);

            // Lưu:
            // - RevokedAt token cũ
            // - Refresh token mới
            await _dbContext.SaveChangesAsync(
                cancellationToken);

            await transaction.CommitAsync(
                cancellationToken);

            return new LoginResponse
            {
                AccessToken =
                    accessToken,

                RefreshToken =
                    newRawRefreshToken,

                TokenType =
                    "Bearer",

                AccessTokenExpiresAt =
                    accessTokenExpiresAt,

                RefreshTokenExpiresAt =
                    newRefreshTokenExpiresAt,

                User =
                    new AuthUserResponse
                    {
                        UserId =
                            user.UserId,

                        Email =
                            user.Email,

                        FullName =
                            user.FullName,

                        Role =
                            user.Role.RoleCode,

                        Status =
                            user.Status,

                        CreatedAt =
                            user.CreatedAt
                    }
            };
        }
        catch
        {
            await transaction.RollbackAsync(
                cancellationToken);

            throw;
        }
    }

    // ======================================================
    // LOGOUT
    // ======================================================

    public async Task LogoutAsync(
        LogoutRequest request,
        CancellationToken cancellationToken = default)
    {
        var tokenHash =
            RefreshTokenHelper.HashToken(
                request.RefreshToken);

        var storedToken =
            await _dbContext.RefreshTokens
                .SingleOrDefaultAsync(
                    x =>
                        x.TokenHash ==
                        tokenHash,
                    cancellationToken);

        // Logout mang tính idempotent:
        // Token không tồn tại thì vẫn xem là đã logout.
        if (storedToken is null)
        {
            return;
        }

        // Nếu chưa revoke thì revoke
        if (!storedToken.RevokedAt.HasValue)
        {
            storedToken.RevokedAt =
                DateTime.UtcNow;

            await _dbContext.SaveChangesAsync(
                cancellationToken);
        }
    }

    // ======================================================
    // VALIDATE COMMON USER DATA
    // ======================================================

    private async Task ValidateCommonUserDataAsync(
        string email,
        string? phone,
        CancellationToken cancellationToken)
    {
        // Kiểm tra Email
        var emailExists =
            await _dbContext.Users
                .AnyAsync(
                    x =>
                        x.Email ==
                        email,
                    cancellationToken);

        if (emailExists)
        {
            throw new ConflictException(
                "EMAIL_ALREADY_EXISTS",
                "Email đã được sử dụng.");
        }

        // Kiểm tra Phone
        if (phone is not null)
        {
            var phoneExists =
                await _dbContext.Users
                    .AnyAsync(
                        x =>
                            x.Phone ==
                            phone,
                        cancellationToken);

            if (phoneExists)
            {
                throw new ConflictException(
                    "PHONE_ALREADY_EXISTS",
                    "Số điện thoại đã được sử dụng.");
            }
        }
    }

    // ======================================================
    // GET ROLE
    // ======================================================

    private async Task<Role> GetRoleAsync(
        string roleCode,
        CancellationToken cancellationToken)
    {
        var role =
            await _dbContext.Roles
                .SingleOrDefaultAsync(
                    x =>
                        x.RoleCode ==
                        roleCode,
                    cancellationToken);

        if (role is null)
        {
            throw new InvalidOperationException(
                $"Role '{roleCode}' chưa được cấu hình trong database.");
        }

        return role;
    }

    // ======================================================
    // CREATE REFRESH TOKEN
    // ======================================================

    private async Task<
        (string Token, DateTime ExpiresAt)>
        CreateRefreshTokenAsync(
            User user,
            CancellationToken cancellationToken)
    {
        // Sinh raw Refresh Token ngẫu nhiên
        var rawToken =
            RefreshTokenHelper
                .GenerateToken();

        // Hash trước khi lưu Database
        var tokenHash =
            RefreshTokenHelper
                .HashToken(
                    rawToken);

        // Refresh Token sống theo số ngày
        // cấu hình trong appsettings.json
        var expiresAt =
            DateTime.UtcNow.AddDays(
                _jwtOptions
                    .RefreshTokenDays);

        var refreshToken =
            new RefreshToken
            {
                UserId =
                    user.UserId,

                TokenHash =
                    tokenHash,

                ExpiresAt =
                    expiresAt,

                RevokedAt =
                    null,

                CreatedAt =
                    DateTime.UtcNow
            };

        _dbContext.RefreshTokens.Add(
            refreshToken);

        // SaveChanges ở đây đồng thời lưu:
        // - RefreshToken
        // - LastLoginAt
        // - PasswordHash nếu rehash
        await _dbContext.SaveChangesAsync(
            cancellationToken);

        // Chỉ trả RAW token cho Client
        return (
            rawToken,
            expiresAt
        );
    }

    // ======================================================
    // NORMALIZE NULLABLE STRING
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

    // ======================================================
    // SQL UNIQUE CONSTRAINT
    // ======================================================

    private static bool IsUniqueConstraintViolation(
        DbUpdateException exception)
    {
        return exception.InnerException
            is SqlException
        {
            Number: 2601 or 2627
        };
    }

    // ======================================================
    // MAP USER RESPONSE
    // ======================================================

    private static AuthUserResponse MapUserResponse(
        User user,
        string roleCode)
    {
        return new AuthUserResponse
        {
            UserId =
                user.UserId,

            Email =
                user.Email,

            FullName =
                user.FullName,

            Role =
                roleCode,

            Status =
                user.Status,

            CreatedAt =
                user.CreatedAt
        };
    }
}