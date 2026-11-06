using System.Data;
using Microsoft.EntityFrameworkCore;
using Roomify.API.Data;
using Roomify.API.DTOs.Contracts;
using Roomify.API.Entities;
using Roomify.API.Exceptions;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Services.Implementations;

public class RentalContractService : IRentalContractService
{
    private readonly RoomifyDbContext _db;

    private static readonly string[] ValidStatuses =
    [
        "PENDING",
        "ACTIVE",
        "COMPLETED",
        "CANCELLED"
    ];

    public RentalContractService(RoomifyDbContext db)
    {
        _db = db;
    }

    // =========================================================
    // LANDLORD - CREATE
    // =========================================================

    public async Task<RentalContractResponse> CreateAsync(
        long userId,
        long rentalRequestId,
        CreateRentalContractRequest request)
    {
        var landlord = await _db.LandlordProfiles
            .FirstOrDefaultAsync(x => x.UserId == userId)
            ?? throw new NotFoundException(
                "LANDLORD_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ chủ trọ.");

        var rentalRequest = await _db.RentalRequests
            .Include(x => x.Room)
                .ThenInclude(x => x.Property)
                    .ThenInclude(x => x.Landlord)
                        .ThenInclude(x => x.User)
            .Include(x => x.Student)
                .ThenInclude(x => x.User)
            .FirstOrDefaultAsync(x =>
                x.RentalRequestId == rentalRequestId &&
                x.Room.Property.LandlordId == landlord.LandlordId);

        if (rentalRequest is null)
        {
            throw new NotFoundException(
                "RENTAL_REQUEST_NOT_FOUND",
                "Không tìm thấy yêu cầu thuê phòng.");
        }

        if (!string.Equals(
                rentalRequest.Status,
                "APPROVED",
                StringComparison.OrdinalIgnoreCase))
        {
            throw new ConflictException(
                "INVALID_RENTAL_REQUEST_STATUS",
                "Chỉ có thể tạo hợp đồng từ yêu cầu thuê đã được chấp thuận.");
        }

        var existingContract = await _db.RentalContracts
            .AnyAsync(x => x.RentalRequestId == rentalRequestId);

        if (existingContract)
        {
            throw new ConflictException(
                "RENTAL_CONTRACT_ALREADY_EXISTS",
                "Yêu cầu thuê này đã có hợp đồng.");
        }

        var room = rentalRequest.Room;

        if (!string.Equals(
                room.AvailabilityStatus,
                "AVAILABLE",
                StringComparison.OrdinalIgnoreCase))
        {
            throw new ConflictException(
                "ROOM_NOT_AVAILABLE",
                "Phòng hiện không còn cho thuê.");
        }

        if (!string.Equals(
                room.ListingStatus,
                "PUBLISHED",
                StringComparison.OrdinalIgnoreCase))
        {
            throw new ConflictException(
                "ROOM_NOT_AVAILABLE",
                "Phòng hiện không ở trạng thái công khai.");
        }

        if (!string.Equals(
                room.Property.Status,
                "ACTIVE",
                StringComparison.OrdinalIgnoreCase))
        {
            throw new ConflictException(
                "PROPERTY_NOT_ACTIVE",
                "Khu trọ hiện không hoạt động.");
        }

        if (!rentalRequest.DurationMonths.HasValue ||
            rentalRequest.DurationMonths.Value <= 0)
        {
            throw new ConflictException(
                "INVALID_RENTAL_DURATION",
                "Thời hạn thuê của yêu cầu không hợp lệ.");
        }

        var startDate = rentalRequest.RequestedMoveInDate;
        var endDate = startDate.AddMonths(
            rentalRequest.DurationMonths.Value);

        var contract = new RentalContract
        {
            RentalRequestId = rentalRequest.RentalRequestId,
            RoomId = rentalRequest.RoomId,

            ContractCode = await GenerateContractCodeAsync(),

            StartDate = startDate,
            EndDate = endDate,

            MonthlyRent = room.RentPrice,
            DepositAmount = room.DepositAmount ?? 0m,

            Terms = NormalizeOptional(request.Terms),
            ContractFileUrl = NormalizeOptional(request.ContractFileUrl),

            Status = "PENDING",
            CreatedAt = DateTime.UtcNow,

            ActivatedAt = null,
            CompletedAt = null,
            CancelledAt = null
        };

        _db.RentalContracts.Add(contract);

        try
        {
            await _db.SaveChangesAsync();
        }
        catch (DbUpdateException)
        {
            throw new ConflictException(
                "RENTAL_CONTRACT_ALREADY_EXISTS",
                "Không thể tạo hợp đồng vì yêu cầu thuê này đã có hợp đồng.");
        }

        return await GetLandlordContractByIdAsync(
            userId,
            contract.ContractId);
    }


    // =========================================================
    // LANDLORD - LIST
    // =========================================================

    public async Task<RentalContractListResponse> GetLandlordContractsAsync(
        long userId,
        string? status,
        int page,
        int pageSize)
    {
        ValidatePagination(page, pageSize);

        var landlord = await _db.LandlordProfiles
            .AsNoTracking()
            .FirstOrDefaultAsync(x => x.UserId == userId)
            ?? throw new NotFoundException(
                "LANDLORD_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ chủ trọ.");

        var normalizedStatus = NormalizeStatus(status);

        var query = BaseQuery()
            .AsNoTracking()
            .Where(x =>
                x.RentalRequest.Room.Property.LandlordId ==
                landlord.LandlordId);

        if (normalizedStatus is not null)
        {
            query = query.Where(x =>
                x.Status == normalizedStatus);
        }

        var totalCount = await query.CountAsync();

        var items = await query
            .OrderByDescending(x => x.CreatedAt)
            .ThenByDescending(x => x.ContractId)
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .ToListAsync();

        return BuildListResponse(
            items.Select(MapResponse).ToList(),
            totalCount,
            page,
            pageSize);
    }


    // =========================================================
    // LANDLORD - DETAIL
    // =========================================================

    public async Task<RentalContractResponse>
        GetLandlordContractByIdAsync(
            long userId,
            long contractId)
    {
        var landlord = await _db.LandlordProfiles
            .AsNoTracking()
            .FirstOrDefaultAsync(x => x.UserId == userId)
            ?? throw new NotFoundException(
                "LANDLORD_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ chủ trọ.");

        var contract = await BaseQuery()
            .AsNoTracking()
            .FirstOrDefaultAsync(x =>
                x.ContractId == contractId &&
                x.RentalRequest.Room.Property.LandlordId ==
                landlord.LandlordId);

        if (contract is null)
        {
            throw new NotFoundException(
                "RENTAL_CONTRACT_NOT_FOUND",
                "Không tìm thấy hợp đồng thuê.");
        }

        return MapResponse(contract);
    }


    // =========================================================
    // STUDENT - LIST
    // =========================================================

    public async Task<RentalContractListResponse> GetStudentContractsAsync(
        long userId,
        string? status,
        int page,
        int pageSize)
    {
        ValidatePagination(page, pageSize);

        var student = await _db.StudentProfiles
            .AsNoTracking()
            .FirstOrDefaultAsync(x => x.UserId == userId)
            ?? throw new NotFoundException(
                "STUDENT_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ sinh viên.");

        var normalizedStatus = NormalizeStatus(status);

        var query = BaseQuery()
            .AsNoTracking()
            .Where(x =>
                x.RentalRequest.StudentId ==
                student.StudentId);

        if (normalizedStatus is not null)
        {
            query = query.Where(x =>
                x.Status == normalizedStatus);
        }

        var totalCount = await query.CountAsync();

        var items = await query
            .OrderByDescending(x => x.CreatedAt)
            .ThenByDescending(x => x.ContractId)
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .ToListAsync();

        return BuildListResponse(
            items.Select(MapResponse).ToList(),
            totalCount,
            page,
            pageSize);
    }


    // =========================================================
    // STUDENT - DETAIL
    // =========================================================

    public async Task<RentalContractResponse>
        GetStudentContractByIdAsync(
            long userId,
            long contractId)
    {
        var student = await _db.StudentProfiles
            .AsNoTracking()
            .FirstOrDefaultAsync(x => x.UserId == userId)
            ?? throw new NotFoundException(
                "STUDENT_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ sinh viên.");

        var contract = await BaseQuery()
            .AsNoTracking()
            .FirstOrDefaultAsync(x =>
                x.ContractId == contractId &&
                x.RentalRequest.StudentId ==
                student.StudentId);

        if (contract is null)
        {
            throw new NotFoundException(
                "RENTAL_CONTRACT_NOT_FOUND",
                "Không tìm thấy hợp đồng thuê.");
        }

        return MapResponse(contract);
    }


    // =========================================================
    // STUDENT - ACTIVATE
    // PENDING -> ACTIVE
    // Room AVAILABLE -> RENTED
    // =========================================================

    public async Task<RentalContractResponse> ActivateAsync(
        long userId,
        long contractId)
    {
        var student = await _db.StudentProfiles
            .FirstOrDefaultAsync(x => x.UserId == userId)
            ?? throw new NotFoundException(
                "STUDENT_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ sinh viên.");

        await using var transaction =
            await _db.Database.BeginTransactionAsync(
                IsolationLevel.Serializable);

        var contract = await BaseQuery()
            .FirstOrDefaultAsync(x =>
                x.ContractId == contractId &&
                x.RentalRequest.StudentId ==
                student.StudentId);

        if (contract is null)
        {
            throw new NotFoundException(
                "RENTAL_CONTRACT_NOT_FOUND",
                "Không tìm thấy hợp đồng thuê.");
        }

        if (!string.Equals(
                contract.Status,
                "PENDING",
                StringComparison.OrdinalIgnoreCase))
        {
            throw new ConflictException(
                "INVALID_RENTAL_CONTRACT_STATUS",
                "Chỉ có thể kích hoạt hợp đồng đang chờ xác nhận.");
        }

        var room = contract.RentalRequest.Room;

        if (!string.Equals(
                room.AvailabilityStatus,
                "AVAILABLE",
                StringComparison.OrdinalIgnoreCase))
        {
            throw new ConflictException(
                "ROOM_NOT_AVAILABLE",
                "Phòng hiện không còn cho thuê.");
        }

        var hasOtherActiveContract =
            await _db.RentalContracts.AnyAsync(x =>
                x.RoomId == contract.RoomId &&
                x.ContractId != contract.ContractId &&
                x.Status == "ACTIVE");

        if (hasOtherActiveContract)
        {
            throw new ConflictException(
                "ROOM_ALREADY_HAS_ACTIVE_CONTRACT",
                "Phòng đã có hợp đồng đang hoạt động.");
        }

        var now = DateTime.UtcNow;

        contract.Status = "ACTIVE";
        contract.ActivatedAt = now;

        room.AvailabilityStatus = "RENTED";
        room.UpdatedAt = now;

        try
        {
            await _db.SaveChangesAsync();
            await transaction.CommitAsync();
        }
        catch (DbUpdateException)
        {
            await transaction.RollbackAsync();

            throw new ConflictException(
                "ROOM_ALREADY_HAS_ACTIVE_CONTRACT",
                "Phòng đã có hợp đồng đang hoạt động.");
        }

        return await GetStudentContractByIdAsync(
            userId,
            contractId);
    }


    // =========================================================
    // LANDLORD - COMPLETE
    // ACTIVE -> COMPLETED
    // Room RENTED -> AVAILABLE
    // =========================================================

    public async Task<RentalContractResponse> CompleteAsync(
    long userId,
    long contractId)
    {
        var landlord = await _db.LandlordProfiles
            .FirstOrDefaultAsync(x => x.UserId == userId)
            ?? throw new NotFoundException(
                "LANDLORD_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ chủ trọ.");

        await using var transaction =
            await _db.Database.BeginTransactionAsync();

        var contract = await BaseQuery()
            .FirstOrDefaultAsync(x =>
                x.ContractId == contractId &&
                x.RentalRequest.Room.Property.LandlordId ==
                landlord.LandlordId);

        if (contract is null)
        {
            throw new NotFoundException(
                "RENTAL_CONTRACT_NOT_FOUND",
                "Không tìm thấy hợp đồng thuê.");
        }

        // Chỉ hợp đồng ACTIVE mới được hoàn thành
        if (!string.Equals(
                contract.Status,
                "ACTIVE",
                StringComparison.OrdinalIgnoreCase))
        {
            throw new ConflictException(
                "INVALID_RENTAL_CONTRACT_STATUS",
                "Chỉ có thể hoàn thành hợp đồng đang hoạt động.");
        }

        // Hợp đồng phải có ngày kết thúc
        if (!contract.EndDate.HasValue)
        {
            throw new ConflictException(
                "RENTAL_CONTRACT_END_DATE_REQUIRED",
                "Hợp đồng chưa có ngày kết thúc.");
        }

        // Không được hoàn thành trước ngày kết thúc hợp đồng
        var today = DateOnly.FromDateTime(DateTime.UtcNow);

        if (today < contract.EndDate.Value)
        {
            throw new ConflictException(
                "RENTAL_CONTRACT_NOT_FINISHED",
                "Chưa thể hoàn thành hợp đồng trước ngày kết thúc.");
        }

        var now = DateTime.UtcNow;

        contract.Status = "COMPLETED";
        contract.CompletedAt = now;

        var room = contract.RentalRequest.Room;

        room.AvailabilityStatus = "AVAILABLE";
        room.UpdatedAt = now;

        await _db.SaveChangesAsync();
        await transaction.CommitAsync();

        return await GetLandlordContractByIdAsync(
            userId,
            contractId);
    }


    // =========================================================
    // STUDENT - CANCEL
    // =========================================================

    public async Task<RentalContractResponse> CancelByStudentAsync(
        long userId,
        long contractId)
    {
        var student = await _db.StudentProfiles
            .FirstOrDefaultAsync(x => x.UserId == userId)
            ?? throw new NotFoundException(
                "STUDENT_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ sinh viên.");

        await using var transaction =
            await _db.Database.BeginTransactionAsync();

        var contract = await BaseQuery()
            .FirstOrDefaultAsync(x =>
                x.ContractId == contractId &&
                x.RentalRequest.StudentId ==
                student.StudentId);

        if (contract is null)
        {
            throw new NotFoundException(
                "RENTAL_CONTRACT_NOT_FOUND",
                "Không tìm thấy hợp đồng thuê.");
        }

        await CancelContractInternalAsync(contract);

        await _db.SaveChangesAsync();
        await transaction.CommitAsync();

        return await GetStudentContractByIdAsync(
            userId,
            contractId);
    }


    // =========================================================
    // LANDLORD - CANCEL
    // =========================================================

    public async Task<RentalContractResponse> CancelByLandlordAsync(
        long userId,
        long contractId)
    {
        var landlord = await _db.LandlordProfiles
            .FirstOrDefaultAsync(x => x.UserId == userId)
            ?? throw new NotFoundException(
                "LANDLORD_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ chủ trọ.");

        await using var transaction =
            await _db.Database.BeginTransactionAsync();

        var contract = await BaseQuery()
            .FirstOrDefaultAsync(x =>
                x.ContractId == contractId &&
                x.RentalRequest.Room.Property.LandlordId ==
                landlord.LandlordId);

        if (contract is null)
        {
            throw new NotFoundException(
                "RENTAL_CONTRACT_NOT_FOUND",
                "Không tìm thấy hợp đồng thuê.");
        }

        await CancelContractInternalAsync(contract);

        await _db.SaveChangesAsync();
        await transaction.CommitAsync();

        return await GetLandlordContractByIdAsync(
            userId,
            contractId);
    }


    // =========================================================
    // PRIVATE HELPERS
    // =========================================================

    private IQueryable<RentalContract> BaseQuery()
    {
        return _db.RentalContracts
            .Include(x => x.RentalRequest)
                .ThenInclude(x => x.Room)
                    .ThenInclude(x => x.Property)
                        .ThenInclude(x => x.Landlord)
                            .ThenInclude(x => x.User)
            .Include(x => x.RentalRequest)
                .ThenInclude(x => x.Student)
                    .ThenInclude(x => x.User);
    }


    private async Task CancelContractInternalAsync(
        RentalContract contract)
    {
        if (contract.Status != "PENDING" &&
            contract.Status != "ACTIVE")
        {
            throw new ConflictException(
                "INVALID_RENTAL_CONTRACT_STATUS",
                "Chỉ có thể hủy hợp đồng đang chờ xác nhận hoặc đang hoạt động.");
        }

        var wasActive = contract.Status == "ACTIVE";

        contract.Status = "CANCELLED";
        contract.CancelledAt = DateTime.UtcNow;

        if (wasActive)
        {
            var room = contract.RentalRequest.Room;

            room.AvailabilityStatus = "AVAILABLE";
            room.UpdatedAt = DateTime.UtcNow;
        }

        await Task.CompletedTask;
    }


    private async Task<string> GenerateContractCodeAsync()
    {
        string contractCode;

        do
        {
            contractCode =
                $"HD-{DateTime.UtcNow:yyyyMMdd}-{Guid.NewGuid()
                    .ToString("N")[..8]
                    .ToUpperInvariant()}";
        }
        while (await _db.RentalContracts
            .AnyAsync(x => x.ContractCode == contractCode));

        return contractCode;
    }


    private static string? NormalizeOptional(string? value)
    {
        return string.IsNullOrWhiteSpace(value)
            ? null
            : value.Trim();
    }


    private static string? NormalizeStatus(string? status)
    {
        if (string.IsNullOrWhiteSpace(status))
        {
            return null;
        }

        var normalized = status.Trim().ToUpperInvariant();

        if (!ValidStatuses.Contains(normalized))
        {
            throw new BadRequestException(
                "INVALID_RENTAL_CONTRACT_STATUS",
                "Trạng thái hợp đồng không hợp lệ.");
        }

        return normalized;
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


    private static RentalContractListResponse BuildListResponse(
        List<RentalContractResponse> items,
        int totalCount,
        int page,
        int pageSize)
    {
        var totalPages =
            totalCount == 0
                ? 0
                : (int)Math.Ceiling(
                    totalCount / (double)pageSize);

        return new RentalContractListResponse
        {
            Items = items,
            TotalCount = totalCount,
            Page = page,
            PageSize = pageSize,
            TotalPages = totalPages,
            HasNextPage = page < totalPages,
            HasPreviousPage = page > 1
        };
    }


    private static RentalContractResponse MapResponse(
        RentalContract contract)
    {
        var rentalRequest = contract.RentalRequest;
        var room = rentalRequest.Room;
        var property = room.Property;
        var student = rentalRequest.Student;
        var studentUser = student.User;
        var landlord = property.Landlord;
        var landlordUser = landlord.User;

        return new RentalContractResponse
        {
            ContractId = contract.ContractId,
            RentalRequestId = contract.RentalRequestId,
            ContractCode = contract.ContractCode,

            RoomId = contract.RoomId,
            RoomTitle = room.Title,

            PropertyId = property.PropertyId,
            PropertyName = property.PropertyName,

            StudentId = student.StudentId,
            StudentName = studentUser.FullName,
            StudentPhone = studentUser.Phone,

            LandlordId = landlord.LandlordId,
            LandlordName = landlordUser.FullName,

            StartDate = contract.StartDate,
            EndDate = contract.EndDate,

            MonthlyRent = contract.MonthlyRent,
            DepositAmount = contract.DepositAmount,

            Terms = contract.Terms,
            ContractFileUrl = contract.ContractFileUrl,

            Status = contract.Status,

            CreatedAt = contract.CreatedAt,
            ActivatedAt = contract.ActivatedAt,
            CompletedAt = contract.CompletedAt,
            CancelledAt = contract.CancelledAt
        };
    }
}