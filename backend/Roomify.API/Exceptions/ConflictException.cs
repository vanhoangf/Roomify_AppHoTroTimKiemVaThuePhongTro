namespace Roomify.API.Exceptions;

public class ConflictException : AppException
{
    public ConflictException(
        string errorCode,
        string message)
        : base(
            StatusCodes.Status409Conflict,
            errorCode,
            message)
    {
    }
}