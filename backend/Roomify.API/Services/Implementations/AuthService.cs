using Microsoft.AspNetCore.Identity;
using Microsoft.Data.SqlClient;
using Microsoft.EntityFrameworkCore;
using Roomify.API.Data;
using Roomify.API.DTOs.Auth;
using Roomify.API.Entities;
using Roomify.API.Exceptions;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Services.Implementations;

public class AuthService : IAuthService
{
    private readonly RoomifyDbContext _dbContext;
    private readonly IPasswordHasher<User> _passwordHasher;
    private readonly IJwtTokenService _jwtTokenService;

    public AuthService(
    RoomifyDbContext dbContext,
    IPasswordHasher<User> passwordHasher,
    IJwtTokenService jwtTokenService)
    {
        _dbContext = dbContext;
        _passwordHasher = passwordHasher;
        _jwtTokenService = jwtTokenService;
    }

    public async Task<AuthUserResponse> RegisterStudentAsync(
        RegisterStudentRequest request,
        CancellationToken cancellationToken = default)
    {
        var email = request.Email.Trim().ToLowerInvariant();
        var fullName = request.FullName.Trim();
        var phone = NormalizeNullable(request.Phone);
        var studentCode = NormalizeNullable(request.StudentCode);

        await ValidateCommonUserDataAsync(
            email,
            phone,
            cancellationToken);

        if (request.UniversityId.HasValue)
        {
            var universityExists =
                await _dbContext.Universities
                    .AnyAsync(
                        x => x.UniversityId == request.UniversityId.Value,
                        cancellationToken);

            if (!universityExists)
            {
                throw new BadRequestException(
                    "UNIVERSITY_NOT_FOUND",
                    "Trường đại học không tồn tại.");
            }
        }

        if (request.UniversityId.HasValue &&
            studentCode is not null)
        {
            var studentCodeExists =
                await _dbContext.StudentProfiles
                    .AnyAsync(
                        x =>
                            x.UniversityId == request.UniversityId.Value &&
                            x.StudentCode == studentCode,
                        cancellationToken);

            if (studentCodeExists)
            {
                throw new ConflictException(
                    "STUDENT_CODE_ALREADY_EXISTS",
                    "Mã sinh viên đã tồn tại tại trường đại học này.");
            }
        }

        var role = await GetRoleAsync(
            "STUDENT",
            cancellationToken);

        await using var transaction =
            await _dbContext.Database.BeginTransactionAsync(
                cancellationToken);

        try
        {
            var user = new User
            {
                RoleId = role.RoleId,
                Email = email,
                PasswordHash = string.Empty,
                FullName = fullName,
                Phone = phone,
                Status = "ACTIVE",
                EmailVerified = false,
                CreatedAt = DateTime.UtcNow,
                UpdatedAt = DateTime.UtcNow
            };

            user.PasswordHash =
                _passwordHasher.HashPassword(
                    user,
                    request.Password);

            _dbContext.Users.Add(user);

            await _dbContext.SaveChangesAsync(
                cancellationToken);

            var studentProfile = new StudentProfile
            {
                UserId = user.UserId,
                UniversityId = request.UniversityId,
                StudentCode = studentCode,
                IsVerified = false,
                CreatedAt = DateTime.UtcNow,
                UpdatedAt = DateTime.UtcNow
            };

            _dbContext.StudentProfiles.Add(studentProfile);

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

    public async Task<AuthUserResponse> RegisterLandlordAsync(
        RegisterLandlordRequest request,
        CancellationToken cancellationToken = default)
    {
        var email = request.Email.Trim().ToLowerInvariant();
        var fullName = request.FullName.Trim();
        var phone = NormalizeNullable(request.Phone);
        var identityNumber =
            NormalizeNullable(request.IdentityNumber);

        await ValidateCommonUserDataAsync(
            email,
            phone,
            cancellationToken);

        if (identityNumber is not null)
        {
            var identityExists =
                await _dbContext.LandlordProfiles
                    .AnyAsync(
                        x => x.IdentityNumber == identityNumber,
                        cancellationToken);

            if (identityExists)
            {
                throw new ConflictException(
                    "IDENTITY_NUMBER_ALREADY_EXISTS",
                    "Số CCCD/giấy tờ đã được sử dụng.");
            }
        }

        var role = await GetRoleAsync(
            "LANDLORD",
            cancellationToken);

        await using var transaction =
            await _dbContext.Database.BeginTransactionAsync(
                cancellationToken);

        try
        {
            var user = new User
            {
                RoleId = role.RoleId,
                Email = email,
                PasswordHash = string.Empty,
                FullName = fullName,
                Phone = phone,
                Status = "ACTIVE",
                EmailVerified = false,
                CreatedAt = DateTime.UtcNow,
                UpdatedAt = DateTime.UtcNow
            };

            user.PasswordHash =
                _passwordHasher.HashPassword(
                    user,
                    request.Password);

            _dbContext.Users.Add(user);

            await _dbContext.SaveChangesAsync(
                cancellationToken);

            var landlordProfile = new LandlordProfile
            {
                UserId = user.UserId,
                IdentityNumber = identityNumber,
                IsVerified = false,
                CreatedAt = DateTime.UtcNow,
                UpdatedAt = DateTime.UtcNow
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

    public async Task<LoginResponse> LoginAsync(
    LoginRequest request,
    CancellationToken cancellationToken = default)
    {
        var email =
            request.Email.Trim().ToLowerInvariant();

        var user = await _dbContext.Users
            .Include(x => x.Role)
            .SingleOrDefaultAsync(
                x => x.Email == email,
                cancellationToken);

        if (user is null)
        {
            throw new UnauthorizedException(
                "INVALID_CREDENTIALS",
                "Email hoặc mật khẩu không chính xác.");
        }

        var passwordResult =
            _passwordHasher.VerifyHashedPassword(
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

        // Chỉ tiết lộ trạng thái LOCKED sau khi password đúng
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

        // Nếu PasswordHasher báo hash cũ cần nâng cấp
        if (passwordResult ==
            PasswordVerificationResult.SuccessRehashNeeded)
        {
            user.PasswordHash =
                _passwordHasher.HashPassword(
                    user,
                    request.Password);
        }

        user.LastLoginAt = DateTime.UtcNow;
        user.UpdatedAt = DateTime.UtcNow;

        await _dbContext.SaveChangesAsync(
            cancellationToken);

        if (user.Role is null)
        {
            throw new InvalidOperationException(
                "Tài khoản chưa được gán vai trò.");
        }

        var (token, expiresAt) =
            _jwtTokenService.GenerateAccessToken(
                user,
                user.Role.RoleCode);

        return new LoginResponse
        {
            AccessToken = token,
            ExpiresAt = expiresAt,

            User = new AuthUserResponse
            {
                UserId = user.UserId,
                Email = user.Email,
                FullName = user.FullName,
                Role = user.Role.RoleCode,
                Status = user.Status,
                CreatedAt = user.CreatedAt
            }
        };
    }

    private async Task ValidateCommonUserDataAsync(
        string email,
        string? phone,
        CancellationToken cancellationToken)
    {
        var emailExists =
            await _dbContext.Users
                .AnyAsync(
                    x => x.Email == email,
                    cancellationToken);

        if (emailExists)
        {
            throw new ConflictException(
                "EMAIL_ALREADY_EXISTS",
                "Email đã được sử dụng.");
        }

        if (phone is not null)
        {
            var phoneExists =
                await _dbContext.Users
                    .AnyAsync(
                        x => x.Phone == phone,
                        cancellationToken);

            if (phoneExists)
            {
                throw new ConflictException(
                    "PHONE_ALREADY_EXISTS",
                    "Số điện thoại đã được sử dụng.");
            }
        }
    }

    private async Task<Role> GetRoleAsync(
        string roleCode,
        CancellationToken cancellationToken)
    {
        var role =
            await _dbContext.Roles
                .SingleOrDefaultAsync(
                    x => x.RoleCode == roleCode,
                    cancellationToken);

        if (role is null)
        {
            throw new InvalidOperationException(
                $"Role '{roleCode}' chưa được cấu hình trong database.");
        }

        return role;
    }

    private static string? NormalizeNullable(
        string? value)
    {
        if (string.IsNullOrWhiteSpace(value))
        {
            return null;
        }

        return value.Trim();
    }

    private static bool IsUniqueConstraintViolation(
        DbUpdateException exception)
    {
        return exception.InnerException is SqlException
        {
            Number: 2601 or 2627
        };
    }

    private static AuthUserResponse MapUserResponse(
        User user,
        string roleCode)
    {
        return new AuthUserResponse
        {
            UserId = user.UserId,
            Email = user.Email,
            FullName = user.FullName,
            Role = roleCode,
            Status = user.Status,
            CreatedAt = user.CreatedAt
        };
    }
}