namespace Roomify.API.Exceptions;

public class NotFoundException : AppException
{
    public NotFoundException(
        string errorCode,
        string message)
        : base(
            StatusCodes.Status404NotFound,
            errorCode,
            message)
    {
    }
}