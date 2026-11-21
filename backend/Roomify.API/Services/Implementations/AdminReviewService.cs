using Microsoft.EntityFrameworkCore;

using Roomify.API.Data;
using Roomify.API.DTOs.Admin.Reviews;
using Roomify.API.Entities;
using Roomify.API.Exceptions;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Services.Implementations;

public class AdminReviewService
    : IAdminReviewService
{
    private readonly RoomifyDbContext _db;


    private static readonly HashSet<string>
        ValidStatuses =
            new(
                StringComparer.OrdinalIgnoreCase)
            {
                "VISIBLE",
                "HIDDEN"
            };


    public AdminReviewService(
        RoomifyDbContext db)
    {
        _db = db;
    }


    // ============================================================
    // GET LIST
    // ============================================================

    public async Task<AdminReviewListResponse>
        GetReviewsAsync(
            string? status,
            int? rating,
            string? search,
            int page,
            int pageSize)
    {
        ValidatePaging(
            page,
            pageSize);


        var normalizedStatus =
            ValidateStatus(status);


        ValidateRating(rating);


        var normalizedSearch =
            NormalizeOptional(search);


        IQueryable<Review> query =
            _db.Reviews
                .AsNoTracking();


        // ========================================================
        // STATUS
        // ========================================================

        if (normalizedStatus is not null)
        {
            query =
                query.Where(x =>
                    x.Status ==
                    normalizedStatus);
        }


        // ========================================================
        // RATING
        // ========================================================

        if (rating.HasValue)
        {
            query =
                query.Where(x =>
                    x.Rating ==
                    rating.Value);
        }


        // ========================================================
        // SEARCH
        // ========================================================

        if (normalizedSearch is not null)
        {
            query =
                query.Where(x =>
                    (x.Comment != null &&
                     x.Comment.Contains(
                         normalizedSearch))
                    ||
                    x.Contract.ContractCode.Contains(
                        normalizedSearch)
                    ||
                    x.Contract.RentalRequest.Room.Title.Contains(
                        normalizedSearch)
                    ||
                    x.Contract.RentalRequest.Room.Property
                        .PropertyName.Contains(
                            normalizedSearch)
                    ||
                    x.Contract.RentalRequest.Student.User
                        .FullName.Contains(
                            normalizedSearch)
                    ||
                    x.Contract.RentalRequest.Student.User
                        .Email.Contains(
                            normalizedSearch));
        }


        // ========================================================
        // FILTERED COUNT
        // ========================================================

        var totalCount =
            await query.CountAsync();


        // ========================================================
        // GLOBAL STATISTICS
        // Không phụ thuộc filter hiện tại.
        // ========================================================

        var statisticsQuery =
            _db.Reviews
                .AsNoTracking();


        var totalReviews =
            await statisticsQuery.CountAsync();


        var visibleReviews =
            await statisticsQuery.CountAsync(
                x =>
                    x.Status == "VISIBLE");


        var hiddenReviews =
            await statisticsQuery.CountAsync(
                x =>
                    x.Status == "HIDDEN");


        var averageRating =
            totalReviews == 0
                ? 0
                : await statisticsQuery
                    .AverageAsync(
                        x =>
                            (double)x.Rating);


        // ========================================================
        // PAGINATION
        // ========================================================

        var items =
            await ProjectResponse(query)
                .OrderByDescending(
                    x => x.CreatedAt)
                .ThenByDescending(
                    x => x.ReviewId)
                .Skip(
                    (page - 1)
                    * pageSize)
                .Take(pageSize)
                .ToListAsync();


        var totalPages =
            totalCount == 0
                ? 0
                : (int)Math.Ceiling(
                    totalCount
                    / (double)pageSize);


        return new AdminReviewListResponse
        {
            Items =
                items,

            TotalCount =
                totalCount,


            TotalReviews =
                totalReviews,

            VisibleReviews =
                visibleReviews,

            HiddenReviews =
                hiddenReviews,

            AverageRating =
                Math.Round(
                    averageRating,
                    2),


            Page =
                page,

            PageSize =
                pageSize,

            TotalPages =
                totalPages,

            HasNextPage =
                page < totalPages,

            HasPreviousPage =
                page > 1
        };
    }


    // ============================================================
    // GET DETAIL
    // ============================================================

    public async Task<AdminReviewResponse>
        GetReviewByIdAsync(
            long reviewId)
    {
        var result =
            await ProjectResponse(
                    _db.Reviews
                        .AsNoTracking()
                        .Where(x =>
                            x.ReviewId ==
                            reviewId))
                .FirstOrDefaultAsync();


        if (result is null)
        {
            throw new NotFoundException(
                "REVIEW_NOT_FOUND",
                "Không tìm thấy đánh giá.");
        }


        return result;
    }


    // ============================================================
    // HIDE REVIEW
    // VISIBLE -> HIDDEN
    // ============================================================

    public async Task<AdminReviewResponse>
        HideAsync(
            long adminUserId,
            long reviewId)
    {
        var review =
            await _db.Reviews
                .FirstOrDefaultAsync(
                    x =>
                        x.ReviewId ==
                        reviewId);


        if (review is null)
        {
            throw new NotFoundException(
                "REVIEW_NOT_FOUND",
                "Không tìm thấy đánh giá.");
        }


        if (string.Equals(
                review.Status,
                "HIDDEN",
                StringComparison.OrdinalIgnoreCase))
        {
            throw new ConflictException(
                "REVIEW_ALREADY_HIDDEN",
                "Đánh giá này đã được ẩn.");
        }


        var now =
            DateTime.UtcNow;


        review.Status =
            "HIDDEN";

        review.ModeratedByUserId =
            adminUserId;

        review.ModeratedAt =
            now;

        review.UpdatedAt =
            now;


        await _db.SaveChangesAsync();


        return await GetReviewByIdAsync(
            reviewId);
    }


    // ============================================================
    // SHOW REVIEW
    // HIDDEN -> VISIBLE
    // ============================================================

    public async Task<AdminReviewResponse>
        ShowAsync(
            long adminUserId,
            long reviewId)
    {
        var review =
            await _db.Reviews
                .FirstOrDefaultAsync(
                    x =>
                        x.ReviewId ==
                        reviewId);


        if (review is null)
        {
            throw new NotFoundException(
                "REVIEW_NOT_FOUND",
                "Không tìm thấy đánh giá.");
        }


        if (string.Equals(
                review.Status,
                "VISIBLE",
                StringComparison.OrdinalIgnoreCase))
        {
            throw new ConflictException(
                "REVIEW_ALREADY_VISIBLE",
                "Đánh giá này đang được hiển thị.");
        }


        var now =
            DateTime.UtcNow;


        review.Status =
            "VISIBLE";

        review.ModeratedByUserId =
            adminUserId;

        review.ModeratedAt =
            now;

        review.UpdatedAt =
            now;


        await _db.SaveChangesAsync();


        return await GetReviewByIdAsync(
            reviewId);
    }


    // ============================================================
    // PROJECTION
    // ============================================================

    private static IQueryable<AdminReviewResponse>
        ProjectResponse(
            IQueryable<Review> query)
    {
        return query.Select(
            x => new AdminReviewResponse
            {
                ReviewId =
                    x.ReviewId,


                ContractId =
                    x.ContractId,

                ContractCode =
                    x.Contract.ContractCode,


                RoomId =
                    x.Contract.RoomId,

                RoomTitle =
                    x.Contract.RentalRequest
                        .Room.Title,


                PropertyId =
                    x.Contract.RentalRequest
                        .Room.PropertyId,

                PropertyName =
                    x.Contract.RentalRequest
                        .Room.Property
                        .PropertyName,


                StudentId =
                    x.Contract.RentalRequest
                        .StudentId,

                StudentUserId =
                    x.Contract.RentalRequest
                        .Student.UserId,

                StudentName =
                    x.Contract.RentalRequest
                        .Student.User
                        .FullName,

                StudentEmail =
                    x.Contract.RentalRequest
                        .Student.User
                        .Email,


                Rating =
                    x.Rating,

                Comment =
                    x.Comment,

                Status =
                    x.Status,


                ModeratedByUserId =
                    x.ModeratedByUserId,

                ModeratedByUserName =
                    x.ModeratedByUser != null
                        ? x.ModeratedByUser
                            .FullName
                        : null,

                ModeratedAt =
                    x.ModeratedAt,


                CreatedAt =
                    x.CreatedAt,

                UpdatedAt =
                    x.UpdatedAt
            });
    }


    // ============================================================
    // STATUS
    // ============================================================

    private static string?
        ValidateStatus(
            string? status)
    {
        var value =
            NormalizeOptional(status);


        if (value is null)
        {
            return null;
        }


        if (!ValidStatuses.Contains(
                value))
        {
            throw new BadRequestException(
                "INVALID_REVIEW_STATUS",
                "Trạng thái đánh giá không hợp lệ.");
        }


        return value.ToUpperInvariant();
    }


    // ============================================================
    // RATING
    // ============================================================

    private static void ValidateRating(
        int? rating)
    {
        if (!rating.HasValue)
        {
            return;
        }


        if (rating.Value < 1 ||
            rating.Value > 5)
        {
            throw new BadRequestException(
                "INVALID_REVIEW_RATING",
                "Điểm đánh giá phải nằm trong khoảng từ 1 đến 5.");
        }
    }


    // ============================================================
    // PAGING
    // ============================================================

    private static void ValidatePaging(
        int page,
        int pageSize)
    {
        if (page < 1 ||
            page > 100000)
        {
            throw new BadRequestException(
                "INVALID_PAGE",
                "Page phải nằm trong khoảng từ 1 đến 100000.");
        }


        if (pageSize < 1 ||
            pageSize > 50)
        {
            throw new BadRequestException(
                "INVALID_PAGE_SIZE",
                "PageSize phải nằm trong khoảng từ 1 đến 50.");
        }
    }


    // ============================================================
    // NORMALIZE
    // ============================================================

    private static string?
        NormalizeOptional(
            string? value)
    {
        if (string.IsNullOrWhiteSpace(
                value))
        {
            return null;
        }


        return value.Trim();
    }
}