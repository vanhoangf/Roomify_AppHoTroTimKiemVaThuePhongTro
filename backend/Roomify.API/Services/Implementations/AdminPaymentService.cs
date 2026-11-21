using Microsoft.EntityFrameworkCore;

using Roomify.API.Data;
using Roomify.API.DTOs.Admin.Payments;
using Roomify.API.Entities;
using Roomify.API.Exceptions;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Services.Implementations;

public class AdminPaymentService
    : IAdminPaymentService
{
    private readonly RoomifyDbContext _db;


    private static readonly HashSet<string>
        ValidStatuses =
            new(
                StringComparer.OrdinalIgnoreCase)
            {
                "PENDING",
                "PAID",
                "FAILED",
                "CANCELLED"
            };


    private static readonly HashSet<string>
        ValidPaymentTypes =
            new(
                StringComparer.OrdinalIgnoreCase)
            {
                "DEPOSIT",
                "RENT",
                "UTILITY",
                "OTHER"
            };


    public AdminPaymentService(
        RoomifyDbContext db)
    {
        _db = db;
    }


    // ============================================================
    // LIST
    // ============================================================

    public async Task<AdminPaymentListResponse>
        GetPaymentsAsync(
            string? status,
            string? paymentType,
            string? search,
            int page,
            int pageSize)
    {
        ValidatePaging(
            page,
            pageSize);


        var normalizedStatus =
            ValidateStatus(
                status);


        var normalizedPaymentType =
            ValidatePaymentType(
                paymentType);


        var normalizedSearch =
            NormalizeOptional(
                search);


        IQueryable<Payment> query =
            _db.Payments
                .AsNoTracking();


        // ========================================================
        // STATUS FILTER
        // ========================================================

        if (normalizedStatus is not null)
        {
            query =
                query.Where(x =>
                    x.Status ==
                    normalizedStatus);
        }


        // ========================================================
        // PAYMENT TYPE FILTER
        // ========================================================

        if (normalizedPaymentType is not null)
        {
            query =
                query.Where(x =>
                    x.PaymentType ==
                    normalizedPaymentType);
        }


        // ========================================================
        // SEARCH
        // ========================================================

        if (normalizedSearch is not null)
        {
            query =
                query.Where(x =>

                    // Transaction
                    (
                        x.TransactionReference != null
                        &&
                        x.TransactionReference.Contains(
                            normalizedSearch)
                    )

                    ||

                    // Contract
                    x.Contract.ContractCode.Contains(
                        normalizedSearch)

                    ||

                    // Room
                    x.Contract
                        .RentalRequest
                        .Room.Title.Contains(
                            normalizedSearch)

                    ||

                    // Property
                    x.Contract
                        .RentalRequest
                        .Room.Property
                        .PropertyName.Contains(
                            normalizedSearch)

                    ||

                    // Student name
                    x.Contract
                        .RentalRequest
                        .Student.User
                        .FullName.Contains(
                            normalizedSearch)

                    ||

                    // Student email
                    x.Contract
                        .RentalRequest
                        .Student.User
                        .Email.Contains(
                            normalizedSearch)

                    ||

                    // Student phone
                    (
                        x.Contract
                            .RentalRequest
                            .Student.User.Phone != null
                        &&
                        x.Contract
                            .RentalRequest
                            .Student.User.Phone.Contains(
                                normalizedSearch)
                    )

                    ||

                    // Landlord name
                    x.Contract
                        .RentalRequest
                        .Room.Property
                        .Landlord.User
                        .FullName.Contains(
                            normalizedSearch)

                    ||

                    // Landlord email
                    x.Contract
                        .RentalRequest
                        .Room.Property
                        .Landlord.User
                        .Email.Contains(
                            normalizedSearch)

                    ||

                    // Landlord phone
                    (
                        x.Contract
                            .RentalRequest
                            .Room.Property
                            .Landlord.User.Phone != null
                        &&
                        x.Contract
                            .RentalRequest
                            .Room.Property
                            .Landlord.User.Phone.Contains(
                                normalizedSearch)
                    ));
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
            _db.Payments
                .AsNoTracking();


        var totalPayments =
            await statisticsQuery
                .CountAsync();


        var pendingPayments =
            await statisticsQuery
                .CountAsync(x =>
                    x.Status == "PENDING");


        var paidPayments =
            await statisticsQuery
                .CountAsync(x =>
                    x.Status == "PAID");


        var failedPayments =
            await statisticsQuery
                .CountAsync(x =>
                    x.Status == "FAILED");


        var cancelledPayments =
            await statisticsQuery
                .CountAsync(x =>
                    x.Status == "CANCELLED");


        var totalPaidAmount =
            await statisticsQuery
                .Where(x =>
                    x.Status == "PAID")
                .SumAsync(x =>
                    (decimal?)x.Amount)
            ?? 0m;


        // ========================================================
        // DATA
        // ========================================================

        var items =
            await ProjectResponse(
                    query)
                .OrderByDescending(x =>
                    x.CreatedAt)
                .ThenByDescending(x =>
                    x.PaymentId)
                .Skip(
                    (page - 1)
                    * pageSize)
                .Take(
                    pageSize)
                .ToListAsync();


        var totalPages =
            totalCount == 0
                ? 0
                : (int)Math.Ceiling(
                    totalCount
                    /
                    (double)pageSize);


        return new AdminPaymentListResponse
        {
            Items =
                items,

            TotalCount =
                totalCount,


            TotalPayments =
                totalPayments,

            PendingPayments =
                pendingPayments,

            PaidPayments =
                paidPayments,

            FailedPayments =
                failedPayments,

            CancelledPayments =
                cancelledPayments,

            TotalPaidAmount =
                totalPaidAmount,


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
    // DETAIL
    // ============================================================

    public async Task<AdminPaymentResponse>
        GetPaymentByIdAsync(
            long paymentId)
    {
        var result =
            await ProjectResponse(
                    _db.Payments
                        .AsNoTracking()
                        .Where(x =>
                            x.PaymentId ==
                            paymentId))
                .FirstOrDefaultAsync();


        if (result is null)
        {
            throw new NotFoundException(
                "PAYMENT_NOT_FOUND",
                "Không tìm thấy khoản thanh toán.");
        }


        return result;
    }


    // ============================================================
    // PROJECTION
    // ============================================================

    private static IQueryable<AdminPaymentResponse>
        ProjectResponse(
            IQueryable<Payment> query)
    {
        return query.Select(
            x =>
                new AdminPaymentResponse
                {
                    PaymentId =
                        x.PaymentId,


                    // CONTRACT
                    ContractId =
                        x.ContractId,

                    ContractCode =
                        x.Contract.ContractCode,


                    // ROOM
                    RoomId =
                        x.Contract
                            .RentalRequest
                            .RoomId,

                    RoomTitle =
                        x.Contract
                            .RentalRequest
                            .Room.Title,


                    // PROPERTY
                    PropertyId =
                        x.Contract
                            .RentalRequest
                            .Room.PropertyId,

                    PropertyName =
                        x.Contract
                            .RentalRequest
                            .Room.Property
                            .PropertyName,


                    // STUDENT
                    StudentId =
                        x.Contract
                            .RentalRequest
                            .StudentId,

                    StudentUserId =
                        x.Contract
                            .RentalRequest
                            .Student.UserId,

                    StudentName =
                        x.Contract
                            .RentalRequest
                            .Student.User
                            .FullName,

                    StudentEmail =
                        x.Contract
                            .RentalRequest
                            .Student.User
                            .Email,

                    StudentPhone =
                        x.Contract
                            .RentalRequest
                            .Student.User
                            .Phone,


                    // LANDLORD
                    LandlordId =
                        x.Contract
                            .RentalRequest
                            .Room.Property
                            .LandlordId,

                    LandlordUserId =
                        x.Contract
                            .RentalRequest
                            .Room.Property
                            .Landlord.UserId,

                    LandlordName =
                        x.Contract
                            .RentalRequest
                            .Room.Property
                            .Landlord.User
                            .FullName,

                    LandlordEmail =
                        x.Contract
                            .RentalRequest
                            .Room.Property
                            .Landlord.User
                            .Email,

                    LandlordPhone =
                        x.Contract
                            .RentalRequest
                            .Room.Property
                            .Landlord.User
                            .Phone,


                    // PAYMENT
                    PaymentType =
                        x.PaymentType,

                    Amount =
                        x.Amount,

                    DueDate =
                        x.DueDate,

                    PaymentMethod =
                        x.PaymentMethod,

                    Status =
                        x.Status,


                    TransactionReference =
                        x.TransactionReference,

                    ProofImageUrl =
                        x.ProofImageUrl,

                    Note =
                        x.Note,


                    // CONFIRM
                    ConfirmedByUserId =
                        x.ConfirmedByUserId,

                    ConfirmedByUserName =
                        x.ConfirmedByUser != null
                            ? x.ConfirmedByUser
                                .FullName
                            : null,

                    ConfirmedAt =
                        x.ConfirmedAt,

                    PaidAt =
                        x.PaidAt,


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
            NormalizeOptional(
                status);


        if (value is null)
        {
            return null;
        }


        if (!ValidStatuses.Contains(
                value))
        {
            throw new BadRequestException(
                "INVALID_PAYMENT_STATUS",
                "Trạng thái thanh toán không hợp lệ.");
        }


        return value.ToUpperInvariant();
    }


    // ============================================================
    // PAYMENT TYPE
    // ============================================================

    private static string?
        ValidatePaymentType(
            string? paymentType)
    {
        var value =
            NormalizeOptional(
                paymentType);


        if (value is null)
        {
            return null;
        }


        if (!ValidPaymentTypes.Contains(
                value))
        {
            throw new BadRequestException(
                "INVALID_PAYMENT_TYPE",
                "Loại thanh toán không hợp lệ.");
        }


        return value.ToUpperInvariant();
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