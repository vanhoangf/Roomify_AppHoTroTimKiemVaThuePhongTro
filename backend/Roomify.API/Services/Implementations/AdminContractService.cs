using Microsoft.EntityFrameworkCore;

using Roomify.API.Data;
using Roomify.API.DTOs.Admin.Contracts;
using Roomify.API.Entities;
using Roomify.API.Exceptions;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Services.Implementations;

public class AdminContractService
    : IAdminContractService
{
    private readonly RoomifyDbContext _db;


    private static readonly HashSet<string>
        ValidStatuses =
            new(
                StringComparer.OrdinalIgnoreCase)
            {
                "PENDING",
                "ACTIVE",
                "COMPLETED",
                "CANCELLED"
            };


    public AdminContractService(
        RoomifyDbContext db)
    {
        _db = db;
    }


    // ============================================================
    // LIST
    // ============================================================

    public async Task<AdminContractListResponse>
        GetContractsAsync(
            string? status,
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


        var normalizedSearch =
            NormalizeOptional(
                search);


        IQueryable<RentalContract> query =
            _db.RentalContracts
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
        // SEARCH
        // ========================================================

        if (normalizedSearch is not null)
        {
            query =
                query.Where(x =>

                    x.ContractCode.Contains(
                        normalizedSearch)

                    ||

                    x.RentalRequest
                        .Room.Title.Contains(
                            normalizedSearch)

                    ||

                    x.RentalRequest
                        .Room.Property
                        .PropertyName.Contains(
                            normalizedSearch)

                    ||

                    x.RentalRequest
                        .Student.User
                        .FullName.Contains(
                            normalizedSearch)

                    ||

                    x.RentalRequest
                        .Student.User
                        .Email.Contains(
                            normalizedSearch)

                    ||

                    (
                        x.RentalRequest
                            .Student.User.Phone != null
                        &&
                        x.RentalRequest
                            .Student.User.Phone.Contains(
                                normalizedSearch)
                    )

                    ||

                    x.RentalRequest
                        .Room.Property
                        .Landlord.User
                        .FullName.Contains(
                            normalizedSearch)

                    ||

                    x.RentalRequest
                        .Room.Property
                        .Landlord.User
                        .Email.Contains(
                            normalizedSearch)

                    ||

                    (
                        x.RentalRequest
                            .Room.Property
                            .Landlord.User.Phone != null
                        &&
                        x.RentalRequest
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
        // Không phụ thuộc filter hiện tại
        // ========================================================

        var statisticsQuery =
            _db.RentalContracts
                .AsNoTracking();


        var totalContracts =
            await statisticsQuery
                .CountAsync();


        var pendingContracts =
            await statisticsQuery
                .CountAsync(x =>
                    x.Status == "PENDING");


        var activeContracts =
            await statisticsQuery
                .CountAsync(x =>
                    x.Status == "ACTIVE");


        var completedContracts =
            await statisticsQuery
                .CountAsync(x =>
                    x.Status == "COMPLETED");


        var cancelledContracts =
            await statisticsQuery
                .CountAsync(x =>
                    x.Status == "CANCELLED");


        // ========================================================
        // ITEMS
        // ========================================================

        var items =
            await ProjectResponse(
                    query)
                .OrderByDescending(x =>
                    x.CreatedAt)
                .ThenByDescending(x =>
                    x.ContractId)
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


        return new AdminContractListResponse
        {
            Items =
                items,

            TotalCount =
                totalCount,


            TotalContracts =
                totalContracts,

            PendingContracts =
                pendingContracts,

            ActiveContracts =
                activeContracts,

            CompletedContracts =
                completedContracts,

            CancelledContracts =
                cancelledContracts,


            Page =
                page,

            PageSize =
                pageSize,

            TotalPages =
                totalPages,

            HasNextPage =
                page <
                totalPages,

            HasPreviousPage =
                page > 1
        };
    }


    // ============================================================
    // DETAIL
    // ============================================================

    public async Task<AdminContractResponse>
        GetContractByIdAsync(
            long contractId)
    {
        var result =
            await ProjectResponse(
                    _db.RentalContracts
                        .AsNoTracking()
                        .Where(x =>
                            x.ContractId ==
                            contractId))
                .FirstOrDefaultAsync();


        if (result is null)
        {
            throw new NotFoundException(
                "RENTAL_CONTRACT_NOT_FOUND",
                "Không tìm thấy hợp đồng thuê.");
        }


        return result;
    }


    // ============================================================
    // PROJECTION
    // ============================================================

    private static IQueryable<AdminContractResponse>
        ProjectResponse(
            IQueryable<RentalContract> query)
    {
        return query.Select(
            x =>
                new AdminContractResponse
                {
                    ContractId =
                        x.ContractId,

                    RentalRequestId =
                        x.RentalRequestId,

                    ContractCode =
                        x.ContractCode,


                    // ROOM
                    RoomId =
                        x.RoomId,

                    RoomTitle =
                        x.RentalRequest
                            .Room.Title,


                    // PROPERTY
                    PropertyId =
                        x.RentalRequest
                            .Room.PropertyId,

                    PropertyName =
                        x.RentalRequest
                            .Room.Property
                            .PropertyName,


                    // STUDENT
                    StudentId =
                        x.RentalRequest
                            .StudentId,

                    StudentUserId =
                        x.RentalRequest
                            .Student.UserId,

                    StudentName =
                        x.RentalRequest
                            .Student.User
                            .FullName,

                    StudentEmail =
                        x.RentalRequest
                            .Student.User
                            .Email,

                    StudentPhone =
                        x.RentalRequest
                            .Student.User
                            .Phone,


                    // LANDLORD
                    LandlordId =
                        x.RentalRequest
                            .Room.Property
                            .LandlordId,

                    LandlordUserId =
                        x.RentalRequest
                            .Room.Property
                            .Landlord.UserId,

                    LandlordName =
                        x.RentalRequest
                            .Room.Property
                            .Landlord.User
                            .FullName,

                    LandlordEmail =
                        x.RentalRequest
                            .Room.Property
                            .Landlord.User
                            .Email,

                    LandlordPhone =
                        x.RentalRequest
                            .Room.Property
                            .Landlord.User
                            .Phone,


                    // CONTRACT
                    StartDate =
                        x.StartDate,

                    EndDate =
                        x.EndDate,

                    MonthlyRent =
                        x.MonthlyRent,

                    DepositAmount =
                        x.DepositAmount,

                    Terms =
                        x.Terms,

                    ContractFileUrl =
                        x.ContractFileUrl,

                    Status =
                        x.Status,


                    // TIMELINE
                    CreatedAt =
                        x.CreatedAt,

                    ActivatedAt =
                        x.ActivatedAt,

                    CompletedAt =
                        x.CompletedAt,

                    CancelledAt =
                        x.CancelledAt
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
                "INVALID_CONTRACT_STATUS",
                "Trạng thái hợp đồng không hợp lệ.");
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
    // NORMALIZATION
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