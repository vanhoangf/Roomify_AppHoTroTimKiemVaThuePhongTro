using Microsoft.EntityFrameworkCore;
using Roomify.API.Data;

var builder = WebApplication.CreateBuilder(args);

// 1. SERVICES

builder.Services.AddControllers();
builder.Services.AddOpenApi();

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

if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
}

app.UseHttpsRedirection();

app.UseAuthorization();

app.MapControllers();

app.Run();