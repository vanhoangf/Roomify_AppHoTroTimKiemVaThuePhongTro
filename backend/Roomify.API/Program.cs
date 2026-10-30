using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Roomify.API.Data;
using Roomify.API.DTOs.Common;
using Roomify.API.Middleware;

var builder = WebApplication.CreateBuilder(args);

// 1. SERVICES

builder.Services
    .AddControllers()
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
                StatusCode =
                    StatusCodes.Status400BadRequest,

                ErrorCode =
                    "VALIDATION_ERROR",

                Message =
                    "Dữ liệu đầu vào không hợp lệ.",

                Errors =
                    errors,

                TraceId =
                    context.HttpContext.TraceIdentifier,

                Timestamp =
                    DateTime.UtcNow
            };

            return new BadRequestObjectResult(response);
        };
    });
builder.Services.AddOpenApi();
builder.Services.AddProblemDetails();
builder.Services.AddExceptionHandler<GlobalExceptionHandler>();

// 2. DATABASE

var connectionString = builder.Configuration
    .GetConnectionString("RoomifyDb")
    ?? throw new InvalidOperationException(
        "Connection string 'RoomifyDb' was not found."
    );

builder.Services.AddDbContext<RoomifyDbContext>(options =>
    options.UseSqlServer(connectionString));

// 3. BUILD APP

var app = builder.Build();

// 4. HTTP PIPELINE
app.UseExceptionHandler();

if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
}

app.UseHttpsRedirection();

app.UseAuthorization();

app.MapControllers();

app.Run();