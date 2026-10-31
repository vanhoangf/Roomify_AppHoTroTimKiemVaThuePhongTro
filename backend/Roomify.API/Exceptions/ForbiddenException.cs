namespace Roomify.API.Exceptions;

public class ForbiddenException : AppException
{
    public ForbiddenException(
        string errorCode,
        string message)
        : base(
            StatusCodes.Status403Forbidden,
            errorCode,
            message)
    {
    }
}