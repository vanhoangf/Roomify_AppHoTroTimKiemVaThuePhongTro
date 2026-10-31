using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using Roomify.API.Data;
using Roomify.API.DTOs.Common;
using Roomify.API.Entities;
using Roomify.API.Middleware;
using Roomify.API.Options;
using Roomify.API.Services.Implementations;
using Roomify.API.Services.Interfaces;
using System.Text;
using System.Text.Json.Serialization;

var builder = WebApplication.CreateBuilder(args);

// 1. CONTROLLERS + VALIDATION

builder.Services
    .AddControllers().AddJsonOptions(options =>
    {
        options.JsonSerializerOptions.UnmappedMemberHandling =
            JsonUnmappedMemberHandling.Disallow;
    })
    .ConfigureApiBehaviorOptions(options =>
    {
        options.InvalidModelStateResponseFactory = context =>
        {
            var errors = context.ModelState
                .Where(x => x.Value?.Errors.Count > 0)
                .ToDictionary(
                    x => x.Key,
                    x => x.Value!.Errors
                        .Select(e =>
                            string.IsNullOrWhiteSpace(e.ErrorMessage)
                                ? "Giá trị không hợp lệ."
                                : e.ErrorMessage)
                        .ToArray()
                );

            var response = new ApiErrorResponse
            {
                StatusCode = StatusCodes.Status400BadRequest,
                ErrorCode = "VALIDATION_ERROR",
                Message = "Dữ liệu đầu vào không hợp lệ.",
                Errors = errors,
                TraceId = context.HttpContext.TraceIdentifier,
                Timestamp = DateTime.UtcNow
            };

            return new BadRequestObjectResult(response);
        };
    });

// 2. OPENAPI

builder.Services.AddOpenApi();

// 3. GLOBAL EXCEPTION HANDLING

builder.Services.AddProblemDetails();

builder.Services.AddExceptionHandler<GlobalExceptionHandler>();

// 4. DATABASE

var connectionString =
    builder.Configuration.GetConnectionString("RoomifyDb")
    ?? throw new InvalidOperationException(
        "Connection string 'RoomifyDb' was not found."
    );

builder.Services.AddDbContext<RoomifyDbContext>(options =>
    options.UseSqlServer(connectionString));

// 5. JWT OPTIONS

builder.Services.Configure<JwtOptions>(
    builder.Configuration.GetSection("Jwt"));

var jwtKey =
    builder.Configuration["Jwt:Key"]
    ?? throw new InvalidOperationException(
        "JWT Key was not found."
    );

var jwtIssuer =
    builder.Configuration["Jwt:Issuer"]
    ?? throw new InvalidOperationException(
        "JWT Issuer was not found."
    );

var jwtAudience =
    builder.Configuration["Jwt:Audience"]
    ?? throw new InvalidOperationException(
        "JWT Audience was not found."
    );

// 6. APPLICATION SERVICES

builder.Services.AddScoped<
    IPasswordHasher<User>,
    PasswordHasher<User>>();

builder.Services.AddScoped<
    IAuthService,
    AuthService>();

builder.Services.AddScoped<
    IJwtTokenService,
    JwtTokenService>();

builder.Services.AddScoped<
    IUserService,
    UserService>();

builder.Services.AddScoped<
    ICatalogService,
    CatalogService>();

builder.Services.AddScoped<
    ILandlordPropertyService,
    LandlordPropertyService>();
// 7. AUTHENTICATION - JWT BEARER

builder.Services
    .AddAuthentication(
        JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        options.MapInboundClaims = false;

        options.TokenValidationParameters =
            new TokenValidationParameters
            {
                ValidateIssuer = true,
                ValidIssuer = jwtIssuer,

                ValidateAudience = true,
                ValidAudience = jwtAudience,

                ValidateLifetime = true,

                ValidateIssuerSigningKey = true,

                IssuerSigningKey =
                    new SymmetricSecurityKey(
                        Encoding.UTF8.GetBytes(jwtKey)),

                ClockSkew = TimeSpan.Zero,

                NameClaimType = "name",

                RoleClaimType = "role"
            };

        options.Events = new JwtBearerEvents
        {
            OnChallenge = async context =>
            {
                // Ngăn middleware trả response 401 mặc định
                context.HandleResponse();

                context.Response.StatusCode =
                    StatusCodes.Status401Unauthorized;

                context.Response.ContentType =
                    "application/json";

                var response = new ApiErrorResponse
                {
                    StatusCode =
                        StatusCodes.Status401Unauthorized,

                    ErrorCode =
                        "UNAUTHORIZED",

                    Message =
                        "Bạn chưa đăng nhập hoặc token không hợp lệ.",

                    Errors = null,

                    TraceId =
                        context.HttpContext.TraceIdentifier,

                    Timestamp =
                        DateTime.UtcNow
                };

                await context.Response.WriteAsJsonAsync(
                    response);
            },

            // Đã đăng nhập nhưng không đúng quyền
            OnForbidden = async context =>
            {
                context.Response.StatusCode =
                    StatusCodes.Status403Forbidden;

                context.Response.ContentType =
                    "application/json";

                var response = new ApiErrorResponse
                {
                    StatusCode =
                        StatusCodes.Status403Forbidden,

                    ErrorCode =
                        "ACCESS_DENIED",

                    Message =
                        "Bạn không có quyền thực hiện chức năng này.",

                    Errors = null,

                    TraceId =
                        context.HttpContext.TraceIdentifier,

                    Timestamp =
                        DateTime.UtcNow
                };

                await context.Response.WriteAsJsonAsync(
                    response);
            }
        };
    });

// 8. AUTHORIZATION

builder.Services.AddAuthorization();

// 9. BUILD APPLICATION

var app = builder.Build();

// 10. HTTP PIPELINE

app.UseExceptionHandler();

if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
}

app.UseHttpsRedirection();

app.UseAuthentication();

app.UseAuthorization();

app.MapControllers();

app.Run();