using System.Security.Claims;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Roomify.Admin.Infrastructure;
using Roomify.Admin.Models.Auth;
using Roomify.Admin.Services.Auth;

namespace Roomify.Admin.Controllers;

public class AuthController : Controller
{
    private readonly IAdminAuthApiService _authService;

    public AuthController(
        IAdminAuthApiService authService)
    {
        _authService = authService;
    }

    // LOGIN - GET

    [AllowAnonymous]
    [HttpGet]
    public IActionResult Login(string? returnUrl = null)
    {
        if (User.Identity?.IsAuthenticated == true &&
            User.IsInRole("ADMIN"))
        {
            return RedirectToAction(
                "Index",
                "Home");
        }

        return View(
            new LoginViewModel
            {
                ReturnUrl = returnUrl
            });
    }
    // LOGIN - POST

    [AllowAnonymous]
    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Login(
        LoginViewModel model,
        CancellationToken cancellationToken)
    {
        if (!ModelState.IsValid)
        {
            return View(model);
        }

        var result =
            await _authService.LoginAsync(
                new LoginRequestDto
                {
                    Email = model.Email.Trim(),
                    Password = model.Password
                },
                cancellationToken);

        if (!result.IsSuccess ||
            result.Data is null)
        {
            ModelState.AddModelError(
                string.Empty,
                result.Message
                    ?? "Không thể đăng nhập.");

            return View(model);
        }

        var login = result.Data;

        // WEB ADMIN CHỈ CHO ROLE ADMIN

        if (!string.Equals(
                login.User.Role,
                "ADMIN",
                StringComparison.OrdinalIgnoreCase))
        {
            ModelState.AddModelError(
                string.Empty,
                "Tài khoản này không có quyền truy cập trang quản trị.");

            return View(model);
        }

        // LƯU JWT / REFRESH TOKEN VÀO SESSION SERVER

        HttpContext.Session.SetString(
            AdminSessionKeys.AccessToken,
            login.AccessToken);

        HttpContext.Session.SetString(
            AdminSessionKeys.RefreshToken,
            login.RefreshToken);

        HttpContext.Session.SetString(
            AdminSessionKeys.AccessTokenExpiresAt,
            login.AccessTokenExpiresAt.ToString("O"));

        HttpContext.Session.SetString(
            AdminSessionKeys.RefreshTokenExpiresAt,
            login.RefreshTokenExpiresAt.ToString("O"));

        HttpContext.Session.SetString(
            AdminSessionKeys.UserId,
            login.User.UserId.ToString());

        HttpContext.Session.SetString(
            AdminSessionKeys.Email,
            login.User.Email);

        HttpContext.Session.SetString(
            AdminSessionKeys.FullName,
            login.User.FullName);

        HttpContext.Session.SetString(
            AdminSessionKeys.Role,
            login.User.Role);

        // TẠO COOKIE PRINCIPAL CHO MVC

        var claims = new List<Claim>
        {
            new(
                ClaimTypes.NameIdentifier,
                login.User.UserId.ToString()),

            new(
                ClaimTypes.Name,
                login.User.FullName),

            new(
                ClaimTypes.Email,
                login.User.Email),

            new(
                ClaimTypes.Role,
                login.User.Role)
        };

        var identity =
            new ClaimsIdentity(
                claims,
                CookieAuthenticationDefaults
                    .AuthenticationScheme);

        var principal =
            new ClaimsPrincipal(identity);

        var properties =
            new AuthenticationProperties
            {
                IsPersistent = model.RememberMe,
                AllowRefresh = true
            };

        if (model.RememberMe)
        {
            properties.ExpiresUtc =
                DateTimeOffset.UtcNow.AddHours(8);
        }

        await HttpContext.SignInAsync(
            CookieAuthenticationDefaults
                .AuthenticationScheme,
            principal,
            properties);

        // RETURN URL
        // Chỉ redirect URL nội bộ để tránh open redirect

        if (!string.IsNullOrWhiteSpace(model.ReturnUrl) &&
            Url.IsLocalUrl(model.ReturnUrl))
        {
            return LocalRedirect(model.ReturnUrl);
        }

        // Tạm thời về Home.
        // Khi làm AD02 sẽ chuyển thành Dashboard.
        return RedirectToAction(
            "Index",
            "Home");
    }

    // LOGOUT

    [Authorize(Roles = "ADMIN")]
    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Logout(
        CancellationToken cancellationToken)
    {
        var refreshToken =
            HttpContext.Session.GetString(
                AdminSessionKeys.RefreshToken);

        // Thử revoke refresh token trên backend.
        if (!string.IsNullOrWhiteSpace(refreshToken))
        {
            await _authService.LogoutAsync(
                refreshToken,
                cancellationToken);
        }

        // Dù backend đang tắt thì vẫn phải logout khỏi Web Admin.
        HttpContext.Session.Clear();

        await HttpContext.SignOutAsync(
            CookieAuthenticationDefaults
                .AuthenticationScheme);

        return RedirectToAction(
            nameof(Login));
    }
    // ACCESS DENIED

    [AllowAnonymous]
    [HttpGet]
    public IActionResult AccessDenied()
    {
        return Content(
            "Bạn không có quyền truy cập chức năng này.");
    }
}