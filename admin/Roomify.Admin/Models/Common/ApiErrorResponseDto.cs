namespace Roomify.Admin.Models.Common;

public class ApiErrorResponseDto
{
    public int StatusCode { get; set; }

    public string ErrorCode { get; set; } = string.Empty;

    public string Message { get; set; } = string.Empty;

    public string TraceId { get; set; } = string.Empty;

    public DateTime Timestamp { get; set; }
}