using Microsoft.EntityFrameworkCore;

using Roomify.API.Data;
using Roomify.API.DTOs.Admin.Statistics;
using Roomify.API.Exceptions;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Services.Implementations;

public class AdminStatisticsService
    : IAdminStatisticsService
{
    private readonly RoomifyDbContext _db;


    public AdminStatisticsService(
        RoomifyDbContext db)
    {
        _db = db;
    }


    // ============================================================
    // GET STATISTICS
    // ============================================================

    public async Task<AdminStatisticsResponse>
        GetStatisticsAsync(
            DateOnly? fromDate,
            DateOnly? toDate)
    {
        var range =
            ResolveDateRange(
                fromDate,
                toDate);


        var from =
            range.FromDate;

        var to =
            range.ToDate;


        var startUtc =
            from.ToDateTime(
                TimeOnly.MinValue,
                DateTimeKind.Utc);


        var endExclusiveUtc =
            to.AddDays(1)
                .ToDateTime(
                    TimeOnly.MinValue,
                    DateTimeKind.Utc);


        // ========================================================
        // OVERVIEW - GLOBAL
        // ========================================================

        var totalUsers =
            await _db.Users
                .AsNoTracking()
                .CountAsync();


        var totalRooms =
            await _db.Rooms
                .AsNoTracking()
                .CountAsync();


        var totalContracts =
            await _db.RentalContracts
                .AsNoTracking()
                .CountAsync();


        var totalPayments =
            await _db.Payments
                .AsNoTracking()
                .CountAsync();


        var globalPaidAmount =
            await _db.Payments
                .AsNoTracking()
                .Where(x =>
                    x.Status == "PAID")
                .SumAsync(x =>
                    (decimal?)x.Amount)
            ?? 0m;


        // ========================================================
        // REVENUE - SELECTED PERIOD
        //
        // Doanh thu dùng PaidAt vì đây là thời điểm payment
        // thực sự được landlord xác nhận PAID.
        // ========================================================

        var periodPaidQuery =
            _db.Payments
                .AsNoTracking()
                .Where(x =>
                    x.Status == "PAID"
                    &&
                    x.PaidAt.HasValue
                    &&
                    x.PaidAt.Value >= startUtc
                    &&
                    x.PaidAt.Value < endExclusiveUtc);


        var periodPaidPayments =
            await periodPaidQuery
                .CountAsync();


        var periodPaidAmount =
            await periodPaidQuery
                .SumAsync(x =>
                    (decimal?)x.Amount)
            ?? 0m;


        var averagePaymentAmount =
            periodPaidPayments == 0
                ? 0m
                : periodPaidAmount
                  / periodPaidPayments;


        var revenueRaw =
            await periodPaidQuery
                .GroupBy(x =>
                    x.PaidAt!.Value.Date)
                .Select(group =>
                    new
                    {
                        Date =
                            group.Key,

                        Amount =
                            group.Sum(x =>
                                x.Amount)
                    })
                .OrderBy(x =>
                    x.Date)
                .ToListAsync();


        var revenueDictionary =
            revenueRaw.ToDictionary(
                x =>
                    DateOnly.FromDateTime(
                        x.Date),
                x =>
                    x.Amount);


        var revenueByDate =
            FillAmountSeries(
                from,
                to,
                revenueDictionary);


        // ========================================================
        // CONTRACTS - GLOBAL STATUS
        // ========================================================

        var contractQuery =
            _db.RentalContracts
                .AsNoTracking();


        var pendingContracts =
            await contractQuery
                .CountAsync(x =>
                    x.Status == "PENDING");


        var activeContracts =
            await contractQuery
                .CountAsync(x =>
                    x.Status == "ACTIVE");


        var completedContracts =
            await contractQuery
                .CountAsync(x =>
                    x.Status == "COMPLETED");


        var cancelledContracts =
            await contractQuery
                .CountAsync(x =>
                    x.Status == "CANCELLED");


        // ========================================================
        // CONTRACTS CREATED IN PERIOD
        // ========================================================

        var periodContractQuery =
            _db.RentalContracts
                .AsNoTracking()
                .Where(x =>
                    x.CreatedAt >= startUtc
                    &&
                    x.CreatedAt < endExclusiveUtc);


        var contractsCreatedInPeriod =
            await periodContractQuery
                .CountAsync();


        var contractsRaw =
            await periodContractQuery
                .GroupBy(x =>
                    x.CreatedAt.Date)
                .Select(group =>
                    new
                    {
                        Date =
                            group.Key,

                        Count =
                            group.Count()
                    })
                .OrderBy(x =>
                    x.Date)
                .ToListAsync();


        var contractsDictionary =
            contractsRaw.ToDictionary(
                x =>
                    DateOnly.FromDateTime(
                        x.Date),
                x =>
                    x.Count);


        var contractsByDate =
            FillCountSeries(
                from,
                to,
                contractsDictionary);


        // ========================================================
        // USERS - GLOBAL
        // ========================================================

        var totalStudents =
            await _db.StudentProfiles
                .AsNoTracking()
                .CountAsync();


        var totalLandlords =
            await _db.LandlordProfiles
                .AsNoTracking()
                .CountAsync();


        // ========================================================
        // USERS CREATED IN PERIOD
        // ========================================================

        var periodUserQuery =
            _db.Users
                .AsNoTracking()
                .Where(x =>
                    x.CreatedAt >= startUtc
                    &&
                    x.CreatedAt < endExclusiveUtc);


        var newUsersInPeriod =
            await periodUserQuery
                .CountAsync();


        var usersRaw =
            await periodUserQuery
                .GroupBy(x =>
                    x.CreatedAt.Date)
                .Select(group =>
                    new
                    {
                        Date =
                            group.Key,

                        Count =
                            group.Count()
                    })
                .OrderBy(x =>
                    x.Date)
                .ToListAsync();


        var usersDictionary =
            usersRaw.ToDictionary(
                x =>
                    DateOnly.FromDateTime(
                        x.Date),
                x =>
                    x.Count);


        var newUsersByDate =
            FillCountSeries(
                from,
                to,
                usersDictionary);


        // ========================================================
        // ROOMS - GLOBAL STATUS
        // ========================================================

        var roomQuery =
            _db.Rooms
                .AsNoTracking();


        var pendingRooms =
            await roomQuery
                .CountAsync(x =>
                    x.ListingStatus == "PENDING");


        var publishedRooms =
            await roomQuery
                .CountAsync(x =>
                    x.ListingStatus == "PUBLISHED");


        var rejectedRooms =
            await roomQuery
                .CountAsync(x =>
                    x.ListingStatus == "REJECTED");


        // ========================================================
        // ROOMS CREATED IN PERIOD
        // ========================================================

        var periodRoomQuery =
            _db.Rooms
                .AsNoTracking()
                .Where(x =>
                    x.CreatedAt >= startUtc
                    &&
                    x.CreatedAt < endExclusiveUtc);


        var roomsCreatedInPeriod =
            await periodRoomQuery
                .CountAsync();


        var roomsRaw =
            await periodRoomQuery
                .GroupBy(x =>
                    x.CreatedAt.Date)
                .Select(group =>
                    new
                    {
                        Date =
                            group.Key,

                        Count =
                            group.Count()
                    })
                .OrderBy(x =>
                    x.Date)
                .ToListAsync();


        var roomsDictionary =
            roomsRaw.ToDictionary(
                x =>
                    DateOnly.FromDateTime(
                        x.Date),
                x =>
                    x.Count);


        var roomsByDate =
            FillCountSeries(
                from,
                to,
                roomsDictionary);


        // ========================================================
        // RESPONSE
        // ========================================================

        return new AdminStatisticsResponse
        {
            FromDate =
                from,

            ToDate =
                to,

            PeriodDays =
                range.PeriodDays,


            Overview =
                new AdminStatisticsOverviewResponse
                {
                    TotalUsers =
                        totalUsers,

                    TotalRooms =
                        totalRooms,

                    TotalContracts =
                        totalContracts,

                    TotalPayments =
                        totalPayments,

                    TotalPaidAmount =
                        globalPaidAmount
                },


            Revenue =
                new AdminRevenueStatisticsResponse
                {
                    PaidPayments =
                        periodPaidPayments,

                    TotalPaidAmount =
                        periodPaidAmount,

                    AveragePaymentAmount =
                        averagePaymentAmount,

                    RevenueByDate =
                        revenueByDate
                },


            Contracts =
                new AdminContractStatisticsResponse
                {
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

                    CreatedInPeriod =
                        contractsCreatedInPeriod,

                    ContractsByDate =
                        contractsByDate
                },


            Users =
                new AdminUserStatisticsResponse
                {
                    TotalUsers =
                        totalUsers,

                    Students =
                        totalStudents,

                    Landlords =
                        totalLandlords,

                    NewUsersInPeriod =
                        newUsersInPeriod,

                    NewUsersByDate =
                        newUsersByDate
                },


            Rooms =
                new AdminRoomStatisticsResponse
                {
                    TotalRooms =
                        totalRooms,

                    PendingRooms =
                        pendingRooms,

                    PublishedRooms =
                        publishedRooms,

                    RejectedRooms =
                        rejectedRooms,

                    CreatedInPeriod =
                        roomsCreatedInPeriod,

                    RoomsByDate =
                        roomsByDate
                }
        };
    }


    // ============================================================
    // DATE RANGE
    // ============================================================

    private static StatisticsDateRange
        ResolveDateRange(
            DateOnly? fromDate,
            DateOnly? toDate)
    {
        var today =
            DateOnly.FromDateTime(
                DateTime.UtcNow);


        DateOnly resolvedTo;
        DateOnly resolvedFrom;


        // Không truyền gì:
        // 30 ngày gần nhất, tính cả hôm nay.
        if (!fromDate.HasValue &&
            !toDate.HasValue)
        {
            resolvedTo =
                today;

            resolvedFrom =
                today.AddDays(-29);
        }

        // Chỉ truyền fromDate:
        // từ fromDate đến hôm nay.
        else if (fromDate.HasValue &&
                 !toDate.HasValue)
        {
            resolvedFrom =
                fromDate.Value;

            resolvedTo =
                today;
        }

        // Chỉ truyền toDate:
        // lấy 30 ngày kết thúc tại toDate.
        else if (!fromDate.HasValue &&
                 toDate.HasValue)
        {
            resolvedTo =
                toDate.Value;

            resolvedFrom =
                resolvedTo.AddDays(-29);
        }

        // Có cả hai.
        else
        {
            resolvedFrom =
                fromDate!.Value;

            resolvedTo =
                toDate!.Value;
        }


        if (resolvedFrom >
            resolvedTo)
        {
            throw new BadRequestException(
                "INVALID_STATISTICS_DATE_RANGE",
                "Từ ngày không được lớn hơn đến ngày.");
        }


        var periodDays =
            resolvedTo.DayNumber
            -
            resolvedFrom.DayNumber
            +
            1;


        if (periodDays > 366)
        {
            throw new BadRequestException(
                "STATISTICS_DATE_RANGE_TOO_LARGE",
                "Khoảng thời gian thống kê tối đa là 366 ngày.");
        }


        return new StatisticsDateRange
        {
            FromDate =
                resolvedFrom,

            ToDate =
                resolvedTo,

            PeriodDays =
                periodDays
        };
    }


    // ============================================================
    // FILL COUNT SERIES
    //
    // Luôn trả đủ từng ngày.
    // Ngày không có dữ liệu => Count = 0.
    // ============================================================

    private static List<AdminStatisticsCountPoint>
        FillCountSeries(
            DateOnly from,
            DateOnly to,
            IReadOnlyDictionary<DateOnly, int> values)
    {
        var result =
            new List<AdminStatisticsCountPoint>();


        for (var date = from;
             date <= to;
             date = date.AddDays(1))
        {
            result.Add(
                new AdminStatisticsCountPoint
                {
                    Date =
                        date,

                    Count =
                        values.TryGetValue(
                            date,
                            out var value)
                            ? value
                            : 0
                });
        }


        return result;
    }


    // ============================================================
    // FILL AMOUNT SERIES
    // ============================================================

    private static List<AdminStatisticsAmountPoint>
        FillAmountSeries(
            DateOnly from,
            DateOnly to,
            IReadOnlyDictionary<DateOnly, decimal> values)
    {
        var result =
            new List<AdminStatisticsAmountPoint>();


        for (var date = from;
             date <= to;
             date = date.AddDays(1))
        {
            result.Add(
                new AdminStatisticsAmountPoint
                {
                    Date =
                        date,

                    Amount =
                        values.TryGetValue(
                            date,
                            out var value)
                            ? value
                            : 0m
                });
        }


        return result;
    }


    // ============================================================
    // INTERNAL RANGE MODEL
    // ============================================================

    private sealed class StatisticsDateRange
    {
        public DateOnly FromDate { get; init; }

        public DateOnly ToDate { get; init; }

        public int PeriodDays { get; init; }
    }
}