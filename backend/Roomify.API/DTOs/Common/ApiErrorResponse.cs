namespace Roomify.API.DTOs.Common;

public class ApiErrorResponse
{
    public bool Success { get; set; } = false;

    public int StatusCode { get; set; }

    public string ErrorCode { get; set; } = string.Empty;

    public string Message { get; set; } = string.Empty;

    public object? Errors { get; set; }

    public string? TraceId { get; set; }

    public DateTime Timestamp { get; set; } = DateTime.UtcNow;
}