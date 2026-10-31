namespace Roomify.API.Exceptions;

public class UnauthorizedException : AppException
{
    public UnauthorizedException(
        string errorCode,
        string message)
        : base(
            StatusCodes.Status401Unauthorized,
            errorCode,
            message)
    {
    }
}