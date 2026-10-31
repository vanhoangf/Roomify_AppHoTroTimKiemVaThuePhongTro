using Microsoft.AspNetCore.Diagnostics;
using Roomify.API.DTOs.Common;
using Roomify.API.Exceptions;

namespace Roomify.API.Middleware;

public class GlobalExceptionHandler
    : IExceptionHandler
{
    private readonly ILogger<GlobalExceptionHandler> _logger;

    public GlobalExceptionHandler(
        ILogger<GlobalExceptionHandler> logger)
    {
        _logger = logger;
    }

    public async ValueTask<bool> TryHandleAsync(
        HttpContext httpContext,
        Exception exception,
        CancellationToken cancellationToken)
    {
        int statusCode;
        string errorCode;
        string message;

        if (exception is AppException appException)
        {
            statusCode = appException.StatusCode;
            errorCode = appException.ErrorCode;
            message = appException.Message;

            _logger.LogWarning(
                exception,
                "Business exception {ErrorCode} - TraceId: {TraceId}",
                errorCode,
                httpContext.TraceIdentifier);
        }
        else
        {
            statusCode = StatusCodes.Status500InternalServerError;
            errorCode = "INTERNAL_SERVER_ERROR";

            message = "Đã xảy ra lỗi trong quá trình xử lý yêu cầu.";

            _logger.LogError(
                exception,
                "Unhandled exception - TraceId: {TraceId}",
                httpContext.TraceIdentifier);
        }

        var response = new ApiErrorResponse
        {
            StatusCode = statusCode,
            ErrorCode = errorCode,
            Message = message,
            TraceId = httpContext.TraceIdentifier,
            Timestamp = DateTime.UtcNow
        };

        httpContext.Response.StatusCode = statusCode;

        httpContext.Response.ContentType =
            "application/json";

        await httpContext.Response.WriteAsJsonAsync(
            response,
            cancellationToken);

        return true;
    }
}