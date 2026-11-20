using Microsoft.AspNetCore.Authentication.Cookies;
using Roomify.Admin.Infrastructure;
using Roomify.Admin.Services.Auth;
using Roomify.Admin.Services.Dashboard;
using Roomify.Admin.Services.Rooms;
using Roomify.Admin.Services.Users;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllersWithViews();

builder.Services.AddDistributedMemoryCache();

builder.Services.AddSession(options =>
{
    options.IdleTimeout = TimeSpan.FromHours(8);

    options.Cookie.Name =
        "Roomify.Admin.Session";

    options.Cookie.HttpOnly = true;
    options.Cookie.IsEssential = true;
});

builder.Services
    .AddAuthentication(
        CookieAuthenticationDefaults.AuthenticationScheme)
    .AddCookie(options =>
    {
        options.LoginPath =
            "/Auth/Login";

        options.AccessDeniedPath =
            "/Auth/AccessDenied";

        options.Cookie.Name =
            "Roomify.Admin.Auth";

        options.ExpireTimeSpan =
            TimeSpan.FromHours(8);

        options.SlidingExpiration = true;

        options.Cookie.HttpOnly = true;
        options.Cookie.IsEssential = true;
    });

builder.Services.AddAuthorization();

// ROOMIFY API CONFIGURATION

var apiBaseUrl =
    builder.Configuration["RoomifyApi:BaseUrl"]
    ?? throw new InvalidOperationException(
        "RoomifyApi:BaseUrl chưa được cấu hình.");
// HTTP CONTEXT

builder.Services.AddHttpContextAccessor();

// JWT HANDLER
builder.Services.AddTransient<
    AdminBearerTokenHandler>();

// AUTH / PUBLIC API
builder.Services.AddHttpClient(
    "RoomifyApi",
    client =>
    {
        client.BaseAddress =
            new Uri(apiBaseUrl);

        client.Timeout =
            TimeSpan.FromSeconds(30);

        client.DefaultRequestHeaders.Add(
            "Accept",
            "application/json");
    });

// ADMIN PROTECTED API
builder.Services
    .AddHttpClient(
        "RoomifyAdminApi",
        client =>
        {
            client.BaseAddress =
                new Uri(apiBaseUrl);

            client.Timeout =
                TimeSpan.FromSeconds(30);

            client.DefaultRequestHeaders.Add(
                "Accept",
                "application/json");
        })
    .AddHttpMessageHandler<
        AdminBearerTokenHandler>();

// SERVICES
builder.Services.AddScoped<IAdminAuthApiService, AdminAuthApiService>();
builder.Services.AddScoped<IAdminDashboardApiService, AdminDashboardApiService>();
builder.Services.AddScoped<IAdminUserApiService, AdminUserApiService>();
builder.Services.AddScoped<IAdminRoomApiService, AdminRoomApiService>();
// APP
var app = builder.Build();

if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler(
        "/Home/Error");

    app.UseHsts();
}

app.UseHttpsRedirection();

app.UseRouting();

app.UseSession();

app.UseAuthentication();
app.UseAuthorization();

app.MapStaticAssets();

app.MapControllerRoute(
        name: "default",
        pattern:
            "{controller=Dashboard}/{action=Index}/{id?}")
    .WithStaticAssets();

app.Run();