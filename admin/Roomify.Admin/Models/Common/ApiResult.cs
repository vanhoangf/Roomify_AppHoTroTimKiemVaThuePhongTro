namespace Roomify.Admin.Models.Common;

public class ApiResult<T>
{
    public bool IsSuccess { get; init; }

    public T? Data { get; init; }

    public int? StatusCode { get; init; }

    public string? ErrorCode { get; init; }

    public string? Message { get; init; }

    public static ApiResult<T> Success(T data)
    {
        return new ApiResult<T>
        {
            IsSuccess = true,
            Data = data
        };
    }

    public static ApiResult<T> Failure(
        string message,
        string? errorCode = null,
        int? statusCode = null)
    {
        return new ApiResult<T>
        {
            IsSuccess = false,
            Message = message,
            ErrorCode = errorCode,
            StatusCode = statusCode
        };
    }
}