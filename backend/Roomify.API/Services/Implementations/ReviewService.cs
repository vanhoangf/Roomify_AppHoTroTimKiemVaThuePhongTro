using Microsoft.EntityFrameworkCore;
using Roomify.API.Data;
using Roomify.API.DTOs.Reviews;
using Roomify.API.Entities;
using Roomify.API.Exceptions;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Services.Implementations;

public class ReviewService : IReviewService
{
    private readonly RoomifyDbContext _db;

    public ReviewService(RoomifyDbContext db)
    {
        _db = db;
    }

    // =========================================================
    // STUDENT - CREATE REVIEW
    // =========================================================

    public async Task<ReviewResponse> CreateAsync(
        long userId,
        long contractId,
        CreateReviewRequest request)
    {
        if (request.Rating < 1 || request.Rating > 5)
        {
            throw new BadRequestException(
                "INVALID_REVIEW_RATING",
                "Điểm đánh giá phải nằm trong khoảng từ 1 đến 5.");
        }

        var contract = await _db.RentalContracts
            .Include(x => x.RentalRequest)
                .ThenInclude(x => x.Student)
            .Include(x => x.RentalRequest)
                .ThenInclude(x => x.Room)
            .FirstOrDefaultAsync(x =>
                x.ContractId == contractId &&
                x.RentalRequest.Student.UserId == userId);

        if (contract is null)
        {
            throw new NotFoundException(
                "RENTAL_CONTRACT_NOT_FOUND",
                "Không tìm thấy hợp đồng thuê.");
        }

        var validStatus =
            string.Equals(
                contract.Status,
                "ACTIVE",
                StringComparison.OrdinalIgnoreCase)
            ||
            string.Equals(
                contract.Status,
                "COMPLETED",
                StringComparison.OrdinalIgnoreCase);

        if (!validStatus)
        {
            throw new ConflictException(
                "INVALID_RENTAL_CONTRACT_STATUS",
                "Chỉ có thể đánh giá hợp đồng đang hoạt động hoặc đã hoàn thành.");
        }

        var reviewExists = await _db.Reviews
            .AnyAsync(x => x.ContractId == contractId);

        if (reviewExists)
        {
            throw new ConflictException(
                "REVIEW_ALREADY_EXISTS",
                "Hợp đồng này đã được đánh giá.");
        }

        var now = DateTime.UtcNow;

        var review = new Review
        {
            ContractId = contract.ContractId,
            Rating = request.Rating,
            Comment = NormalizeOptional(request.Comment),

            Status = "VISIBLE",

            ModeratedByUserId = null,
            ModeratedAt = null,

            CreatedAt = now,
            UpdatedAt = now
        };

        _db.Reviews.Add(review);

        try
        {
            await _db.SaveChangesAsync();
        }
        catch (DbUpdateException)
        {
            var duplicate = await _db.Reviews
                .AsNoTracking()
                .AnyAsync(x => x.ContractId == contractId);

            if (duplicate)
            {
                throw new ConflictException(
                    "REVIEW_ALREADY_EXISTS",
                    "Hợp đồng này đã được đánh giá.");
            }

            throw;
        }

        return await GetStudentReviewByContractAsync(
            userId,
            contractId);
    }


    // =========================================================
    // STUDENT - GET REVIEW BY CONTRACT
    // =========================================================

    public async Task<ReviewResponse> GetStudentReviewByContractAsync(
        long userId,
        long contractId)
    {
        var review = await BaseReviewQuery()
            .Where(x =>
                x.ContractId == contractId &&
                x.Contract.RentalRequest.Student.UserId == userId)
            .Select(x => new ReviewResponse
            {
                ReviewId = x.ReviewId,
                ContractId = x.ContractId,

                RoomId = x.Contract.RoomId,
                RoomTitle = x.Contract.RentalRequest.Room.Title,

                StudentId =
                    x.Contract.RentalRequest.StudentId,

                StudentName =
                    x.Contract.RentalRequest.Student.User.FullName,

                Rating = x.Rating,
                Comment = x.Comment,
                Status = x.Status,

                CreatedAt = x.CreatedAt,
                UpdatedAt = x.UpdatedAt
            })
            .FirstOrDefaultAsync();

        if (review is null)
        {
            throw new NotFoundException(
                "REVIEW_NOT_FOUND",
                "Không tìm thấy đánh giá.");
        }

        return review;
    }


    // =========================================================
    // PUBLIC - ROOM REVIEWS
    // =========================================================

    public async Task<ReviewListResponse> GetRoomReviewsAsync(
        long roomId,
        int page,
        int pageSize)
    {
        ValidatePagination(page, pageSize);

        var roomExists = await _db.Rooms
            .AsNoTracking()
            .AnyAsync(x =>
                x.RoomId == roomId &&
                x.ListingStatus == "PUBLISHED");

        if (!roomExists)
        {
            throw new NotFoundException(
                "ROOM_NOT_FOUND",
                "Không tìm thấy phòng công khai.");
        }

        var query = BaseReviewQuery()
            .Where(x =>
                x.Contract.RoomId == roomId &&
                x.Status == "VISIBLE");

        var totalCount = await query.CountAsync();

        var averageRating = totalCount == 0
            ? 0
            : await query.AverageAsync(x => (double)x.Rating);

        var items = await query
            .OrderByDescending(x => x.CreatedAt)
            .ThenByDescending(x => x.ReviewId)
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .Select(x => new ReviewResponse
            {
                ReviewId = x.ReviewId,
                ContractId = x.ContractId,

                RoomId = x.Contract.RoomId,
                RoomTitle = x.Contract.RentalRequest.Room.Title,

                StudentId =
                    x.Contract.RentalRequest.StudentId,

                StudentName =
                    x.Contract.RentalRequest.Student.User.FullName,

                Rating = x.Rating,
                Comment = x.Comment,
                Status = x.Status,

                CreatedAt = x.CreatedAt,
                UpdatedAt = x.UpdatedAt
            })
            .ToListAsync();

        var totalPages =
            totalCount == 0
                ? 0
                : (int)Math.Ceiling(
                    totalCount / (double)pageSize);

        return new ReviewListResponse
        {
            Items = items,

            TotalCount = totalCount,
            AverageRating =
                Math.Round(averageRating, 2),

            Page = page,
            PageSize = pageSize,
            TotalPages = totalPages,

            HasNextPage = page < totalPages,
            HasPreviousPage = page > 1
        };
    }


    // =========================================================
    // BASE QUERY
    // =========================================================

    private IQueryable<Review> BaseReviewQuery()
    {
        return _db.Reviews
            .AsNoTracking();
    }


    // =========================================================
    // HELPERS
    // =========================================================

    private static string? NormalizeOptional(string? value)
    {
        if (string.IsNullOrWhiteSpace(value))
        {
            return null;
        }

        return value.Trim();
    }

    private static void ValidatePagination(
        int page,
        int pageSize)
    {
        if (page < 1 || page > 100000)
        {
            throw new BadRequestException(
                "INVALID_PAGE",
                "Page phải nằm trong khoảng từ 1 đến 100000.");
        }

        if (pageSize < 1 || pageSize > 50)
        {
            throw new BadRequestException(
                "INVALID_PAGE_SIZE",
                "PageSize phải nằm trong khoảng từ 1 đến 50.");
        }
    }
}