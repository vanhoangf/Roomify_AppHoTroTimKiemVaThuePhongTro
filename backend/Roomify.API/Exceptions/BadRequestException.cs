namespace Roomify.API.Exceptions;

public class BadRequestException : AppException
{
    public BadRequestException(
        string errorCode,
        string message)
        : base(
            StatusCodes.Status400BadRequest,
            errorCode,
            message)
    {
    }
}