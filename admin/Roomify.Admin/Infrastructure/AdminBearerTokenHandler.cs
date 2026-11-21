using System.Net.Http.Headers;

namespace Roomify.Admin.Infrastructure;

public class AdminBearerTokenHandler : DelegatingHandler
{
    private readonly IHttpContextAccessor
        _httpContextAccessor;

    private readonly ILogger<AdminBearerTokenHandler>
        _logger;


    public AdminBearerTokenHandler(
        IHttpContextAccessor httpContextAccessor,
        ILogger<AdminBearerTokenHandler> logger)
    {
        _httpContextAccessor =
            httpContextAccessor;

        _logger = logger;
    }


    protected override async Task<HttpResponseMessage>
        SendAsync(
            HttpRequestMessage request,
            CancellationToken cancellationToken)
    {
        var context =
            _httpContextAccessor.HttpContext;


        if (context is null)
        {
            _logger.LogWarning(
                "Không tìm thấy HttpContext khi gọi Admin API.");

            return await base.SendAsync(
                request,
                cancellationToken);
        }


        var accessToken =
            context.Session.GetString(
                AdminSessionKeys.AccessToken);


        if (string.IsNullOrWhiteSpace(accessToken))
        {
            _logger.LogWarning(
                "Không tìm thấy AccessToken trong Admin Session.");
        }
        else
        {
            accessToken =
                accessToken.Trim();

            request.Headers.Authorization =
                new AuthenticationHeaderValue(
                    "Bearer",
                    accessToken);

            _logger.LogInformation(
                "Đã gắn Bearer Token cho Admin API. TokenLength: {TokenLength}",
                accessToken.Length);
        }


        return await base.SendAsync(
            request,
            cancellationToken);
    }
}