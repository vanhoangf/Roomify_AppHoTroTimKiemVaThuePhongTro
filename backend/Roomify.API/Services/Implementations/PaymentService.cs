using Microsoft.EntityFrameworkCore;
using Roomify.API.Data;
using Roomify.API.DTOs.Payments;
using Roomify.API.Entities;
using Roomify.API.Exceptions;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Services.Implementations;

public class PaymentService : IPaymentService
{
    private readonly RoomifyDbContext _db;

    private static readonly HashSet<string> ValidPaymentTypes =
        new(StringComparer.OrdinalIgnoreCase)
        {
            "DEPOSIT",
            "RENT",
            "UTILITY",
            "OTHER"
        };

    private static readonly HashSet<string> ValidPaymentMethods =
        new(StringComparer.OrdinalIgnoreCase)
        {
            "CASH",
            "BANK_TRANSFER",
            "OTHER"
        };

    private static readonly HashSet<string> ValidStatuses =
        new(StringComparer.OrdinalIgnoreCase)
        {
            "PENDING",
            "PAID",
            "FAILED",
            "CANCELLED"
        };

    public PaymentService(RoomifyDbContext db)
    {
        _db = db;
    }

    // ============================================================
    // LANDLORD - CREATE
    // ============================================================

    public async Task<PaymentResponse> CreateAsync(
        long userId,
        long contractId,
        CreatePaymentRequest request)
    {
        var landlord = await _db.LandlordProfiles
            .FirstOrDefaultAsync(x => x.UserId == userId)
            ?? throw new NotFoundException(
                "LANDLORD_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ chủ trọ.");

        var contract = await _db.RentalContracts
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

        if (!string.Equals(
                contract.Status,
                "ACTIVE",
                StringComparison.OrdinalIgnoreCase))
        {
            throw new ConflictException(
                "INVALID_RENTAL_CONTRACT_STATUS",
                "Chỉ có thể tạo khoản thanh toán cho hợp đồng đang hoạt động.");
        }

        var paymentType = NormalizeRequired(request.PaymentType);

        if (!ValidPaymentTypes.Contains(paymentType))
        {
            throw new BadRequestException(
                "INVALID_PAYMENT_TYPE",
                "Loại thanh toán không hợp lệ.");
        }

        if (request.Amount <= 0)
        {
            throw new BadRequestException(
                "INVALID_PAYMENT_AMOUNT",
                "Số tiền thanh toán phải lớn hơn 0.");
        }

        var now = DateTime.UtcNow;

        var payment = new Payment
        {
            ContractId = contract.ContractId,
            PaymentType = paymentType.ToUpperInvariant(),
            Amount = request.Amount,
            DueDate = request.DueDate,
            PaymentMethod = null,
            Status = "PENDING",
            TransactionReference = null,
            ProofImageUrl = null,
            Note = NormalizeOptional(request.Note),
            ConfirmedByUserId = null,
            ConfirmedAt = null,
            PaidAt = null,
            CreatedAt = now,
            UpdatedAt = now
        };

        _db.Payments.Add(payment);
        await _db.SaveChangesAsync();

        return await GetLandlordPaymentByIdAsync(
            userId,
            payment.PaymentId);
    }

    // ============================================================
    // LANDLORD - LIST
    // ============================================================

    public async Task<PaymentListResponse> GetLandlordPaymentsAsync(
        long userId,
        string? status,
        string? paymentType,
        int page,
        int pageSize)
    {
        ValidatePaging(page, pageSize);

        var landlord = await _db.LandlordProfiles
            .AsNoTracking()
            .FirstOrDefaultAsync(x => x.UserId == userId)
            ?? throw new NotFoundException(
                "LANDLORD_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ chủ trọ.");

        var normalizedStatus = ValidateStatusFilter(status);
        var normalizedPaymentType =
            ValidatePaymentTypeFilter(paymentType);

        var query = _db.Payments
            .AsNoTracking()
            .Where(x =>
                x.Contract.RentalRequest.Room.Property.LandlordId ==
                landlord.LandlordId);

        if (normalizedStatus is not null)
        {
            query = query.Where(x =>
                x.Status == normalizedStatus);
        }

        if (normalizedPaymentType is not null)
        {
            query = query.Where(x =>
                x.PaymentType == normalizedPaymentType);
        }

        var totalCount = await query.CountAsync();

        var items = await ProjectResponse(query)
            .OrderByDescending(x => x.CreatedAt)
            .ThenByDescending(x => x.PaymentId)
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .ToListAsync();

        return BuildListResponse(
            items,
            totalCount,
            page,
            pageSize);
    }

    // ============================================================
    // LANDLORD - DETAIL
    // ============================================================

    public async Task<PaymentResponse> GetLandlordPaymentByIdAsync(
        long userId,
        long paymentId)
    {
        var landlord = await _db.LandlordProfiles
            .AsNoTracking()
            .FirstOrDefaultAsync(x => x.UserId == userId)
            ?? throw new NotFoundException(
                "LANDLORD_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ chủ trọ.");

        var response = await ProjectResponse(
                _db.Payments
                    .AsNoTracking()
                    .Where(x =>
                        x.PaymentId == paymentId &&
                        x.Contract.RentalRequest.Room.Property.LandlordId ==
                        landlord.LandlordId))
            .FirstOrDefaultAsync();

        if (response is null)
        {
            throw new NotFoundException(
                "PAYMENT_NOT_FOUND",
                "Không tìm thấy khoản thanh toán.");
        }

        return response;
    }

    // ============================================================
    // LANDLORD - CONFIRM
    // ============================================================

    public async Task<PaymentResponse> ConfirmAsync(
        long userId,
        long paymentId,
        ConfirmPaymentRequest request)
    {
        var landlord = await _db.LandlordProfiles
            .FirstOrDefaultAsync(x => x.UserId == userId)
            ?? throw new NotFoundException(
                "LANDLORD_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ chủ trọ.");

        await using var transaction =
            await _db.Database.BeginTransactionAsync();

        var payment = await _db.Payments
            .FirstOrDefaultAsync(x =>
                x.PaymentId == paymentId &&
                x.Contract.RentalRequest.Room.Property.LandlordId ==
                landlord.LandlordId);

        if (payment is null)
        {
            throw new NotFoundException(
                "PAYMENT_NOT_FOUND",
                "Không tìm thấy khoản thanh toán.");
        }

        EnsurePending(payment);

        var now = DateTime.UtcNow;

        payment.Status = "PAID";

        payment.ConfirmedByUserId = userId;
        payment.ConfirmedAt = now;
        payment.PaidAt = now;

        var note = NormalizeOptional(request.Note);

        if (note is not null)
        {
            payment.Note = note;
        }

        payment.UpdatedAt = now;

        await _db.SaveChangesAsync();
        await transaction.CommitAsync();

        return await GetLandlordPaymentByIdAsync(
            userId,
            paymentId);
    }

    // ============================================================
    // LANDLORD - REJECT
    // ============================================================

    public async Task<PaymentResponse> RejectAsync(
        long userId,
        long paymentId,
        RejectPaymentRequest request)
    {
        var landlord = await _db.LandlordProfiles
            .FirstOrDefaultAsync(x => x.UserId == userId)
            ?? throw new NotFoundException(
                "LANDLORD_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ chủ trọ.");

        var payment = await _db.Payments
            .FirstOrDefaultAsync(x =>
                x.PaymentId == paymentId &&
                x.Contract.RentalRequest.Room.Property.LandlordId ==
                landlord.LandlordId);

        if (payment is null)
        {
            throw new NotFoundException(
                "PAYMENT_NOT_FOUND",
                "Không tìm thấy khoản thanh toán.");
        }

        EnsurePending(payment);

        var note = NormalizeOptional(request.Note);

        if (note is null)
        {
            throw new BadRequestException(
                "PAYMENT_REJECTION_REASON_REQUIRED",
                "Vui lòng nhập lý do từ chối thanh toán.");
        }

        var now = DateTime.UtcNow;

        payment.Status = "FAILED";
        payment.Note = note;
        payment.UpdatedAt = now;

        await _db.SaveChangesAsync();

        return await GetLandlordPaymentByIdAsync(
            userId,
            paymentId);
    }

    // ============================================================
    // LANDLORD - CANCEL
    // ============================================================

    public async Task<PaymentResponse> CancelByLandlordAsync(
        long userId,
        long paymentId)
    {
        var landlord = await _db.LandlordProfiles
            .FirstOrDefaultAsync(x => x.UserId == userId)
            ?? throw new NotFoundException(
                "LANDLORD_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ chủ trọ.");

        var payment = await _db.Payments
            .FirstOrDefaultAsync(x =>
                x.PaymentId == paymentId &&
                x.Contract.RentalRequest.Room.Property.LandlordId ==
                landlord.LandlordId);

        if (payment is null)
        {
            throw new NotFoundException(
                "PAYMENT_NOT_FOUND",
                "Không tìm thấy khoản thanh toán.");
        }

        EnsurePending(payment);

        payment.Status = "CANCELLED";
        payment.UpdatedAt = DateTime.UtcNow;

        await _db.SaveChangesAsync();

        return await GetLandlordPaymentByIdAsync(
            userId,
            paymentId);
    }

    // ============================================================
    // STUDENT - LIST
    // ============================================================

    public async Task<PaymentListResponse> GetStudentPaymentsAsync(
        long userId,
        string? status,
        string? paymentType,
        int page,
        int pageSize)
    {
        ValidatePaging(page, pageSize);

        var student = await _db.StudentProfiles
            .AsNoTracking()
            .FirstOrDefaultAsync(x => x.UserId == userId)
            ?? throw new NotFoundException(
                "STUDENT_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ sinh viên.");

        var normalizedStatus = ValidateStatusFilter(status);
        var normalizedPaymentType =
            ValidatePaymentTypeFilter(paymentType);

        var query = _db.Payments
            .AsNoTracking()
            .Where(x =>
                x.Contract.RentalRequest.StudentId ==
                student.StudentId);

        if (normalizedStatus is not null)
        {
            query = query.Where(x =>
                x.Status == normalizedStatus);
        }

        if (normalizedPaymentType is not null)
        {
            query = query.Where(x =>
                x.PaymentType == normalizedPaymentType);
        }

        var totalCount = await query.CountAsync();

        var items = await ProjectResponse(query)
            .OrderByDescending(x => x.CreatedAt)
            .ThenByDescending(x => x.PaymentId)
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .ToListAsync();

        return BuildListResponse(
            items,
            totalCount,
            page,
            pageSize);
    }

    // ============================================================
    // STUDENT - DETAIL
    // ============================================================

    public async Task<PaymentResponse> GetStudentPaymentByIdAsync(
        long userId,
        long paymentId)
    {
        var student = await _db.StudentProfiles
            .AsNoTracking()
            .FirstOrDefaultAsync(x => x.UserId == userId)
            ?? throw new NotFoundException(
                "STUDENT_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ sinh viên.");

        var response = await ProjectResponse(
                _db.Payments
                    .AsNoTracking()
                    .Where(x =>
                        x.PaymentId == paymentId &&
                        x.Contract.RentalRequest.StudentId ==
                        student.StudentId))
            .FirstOrDefaultAsync();

        if (response is null)
        {
            throw new NotFoundException(
                "PAYMENT_NOT_FOUND",
                "Không tìm thấy khoản thanh toán.");
        }

        return response;
    }

    // ============================================================
    // STUDENT - SUBMIT PAYMENT
    // ============================================================

    public async Task<PaymentResponse> SubmitAsync(
        long userId,
        long paymentId,
        SubmitPaymentRequest request)
    {
        var student = await _db.StudentProfiles
            .FirstOrDefaultAsync(x => x.UserId == userId)
            ?? throw new NotFoundException(
                "STUDENT_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ sinh viên.");

        var payment = await _db.Payments
            .FirstOrDefaultAsync(x =>
                x.PaymentId == paymentId &&
                x.Contract.RentalRequest.StudentId ==
                student.StudentId);

        if (payment is null)
        {
            throw new NotFoundException(
                "PAYMENT_NOT_FOUND",
                "Không tìm thấy khoản thanh toán.");
        }

        EnsurePending(payment);

        var paymentMethod =
            NormalizeRequired(request.PaymentMethod);

        if (!ValidPaymentMethods.Contains(paymentMethod))
        {
            throw new BadRequestException(
                "INVALID_PAYMENT_METHOD",
                "Phương thức thanh toán không hợp lệ.");
        }

        var now = DateTime.UtcNow;

        payment.PaymentMethod =
            paymentMethod.ToUpperInvariant();

        payment.TransactionReference =
            NormalizeOptional(request.TransactionReference);

        payment.ProofImageUrl =
            NormalizeOptional(request.ProofImageUrl);

        var note = NormalizeOptional(request.Note);

        if (note is not null)
        {
            payment.Note = note;
        }

        // Không có trạng thái SUBMITTED trong database.
        // Sau khi sinh viên gửi minh chứng, payment vẫn PENDING
        // cho đến khi landlord xác nhận hoặc từ chối.
        payment.Status = "PENDING";

        payment.UpdatedAt = now;

        await _db.SaveChangesAsync();

        return await GetStudentPaymentByIdAsync(
            userId,
            paymentId);
    }

    // ============================================================
    // STUDENT - CANCEL
    // ============================================================

    public async Task<PaymentResponse> CancelByStudentAsync(
        long userId,
        long paymentId)
    {
        var student = await _db.StudentProfiles
            .FirstOrDefaultAsync(x => x.UserId == userId)
            ?? throw new NotFoundException(
                "STUDENT_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ sinh viên.");

        var payment = await _db.Payments
            .FirstOrDefaultAsync(x =>
                x.PaymentId == paymentId &&
                x.Contract.RentalRequest.StudentId ==
                student.StudentId);

        if (payment is null)
        {
            throw new NotFoundException(
                "PAYMENT_NOT_FOUND",
                "Không tìm thấy khoản thanh toán.");
        }

        EnsurePending(payment);

        payment.Status = "CANCELLED";
        payment.UpdatedAt = DateTime.UtcNow;

        await _db.SaveChangesAsync();

        return await GetStudentPaymentByIdAsync(
            userId,
            paymentId);
    }

    // ============================================================
    // PROJECTION
    // ============================================================

    private static IQueryable<PaymentResponse> ProjectResponse(
        IQueryable<Payment> query)
    {
        return query.Select(x => new PaymentResponse
        {
            PaymentId = x.PaymentId,

            ContractId = x.ContractId,
            ContractCode = x.Contract.ContractCode,

            RoomId = x.Contract.RentalRequest.RoomId,
            RoomTitle = x.Contract.RentalRequest.Room.Title,

            StudentId = x.Contract.RentalRequest.StudentId,

            StudentName =
                x.Contract.RentalRequest.Student.User.FullName,

            StudentPhone =
                x.Contract.RentalRequest.Student.User.Phone,

            LandlordId =
                x.Contract.RentalRequest.Room.Property.LandlordId,

            LandlordName =
                x.Contract.RentalRequest.Room.Property
                    .Landlord.User.FullName,

            PaymentType = x.PaymentType,
            Amount = x.Amount,
            DueDate = x.DueDate,

            PaymentMethod = x.PaymentMethod,
            Status = x.Status,

            TransactionReference =
                x.TransactionReference,

            ProofImageUrl =
                x.ProofImageUrl,

            Note = x.Note,

            ConfirmedByUserId =
                x.ConfirmedByUserId,

            ConfirmedByUserName =
                x.ConfirmedByUser != null
                    ? x.ConfirmedByUser.FullName
                    : null,

            ConfirmedAt = x.ConfirmedAt,
            PaidAt = x.PaidAt,

            CreatedAt = x.CreatedAt,
            UpdatedAt = x.UpdatedAt
        });
    }

    // ============================================================
    // VALIDATION
    // ============================================================

    private static void EnsurePending(Payment payment)
    {
        if (!string.Equals(
                payment.Status,
                "PENDING",
                StringComparison.OrdinalIgnoreCase))
        {
            throw new ConflictException(
                "INVALID_PAYMENT_STATUS",
                "Chỉ có thể xử lý khoản thanh toán đang chờ.");
        }
    }

    private static string? ValidateStatusFilter(
        string? status)
    {
        var value = NormalizeOptional(status);

        if (value is null)
        {
            return null;
        }

        if (!ValidStatuses.Contains(value))
        {
            throw new BadRequestException(
                "INVALID_PAYMENT_STATUS",
                "Trạng thái thanh toán không hợp lệ.");
        }

        return value.ToUpperInvariant();
    }

    private static string? ValidatePaymentTypeFilter(
        string? paymentType)
    {
        var value = NormalizeOptional(paymentType);

        if (value is null)
        {
            return null;
        }

        if (!ValidPaymentTypes.Contains(value))
        {
            throw new BadRequestException(
                "INVALID_PAYMENT_TYPE",
                "Loại thanh toán không hợp lệ.");
        }

        return value.ToUpperInvariant();
    }

    private static void ValidatePaging(
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

    // ============================================================
    // LIST RESPONSE
    // ============================================================

    private static PaymentListResponse BuildListResponse(
        List<PaymentResponse> items,
        int totalCount,
        int page,
        int pageSize)
    {
        var totalPages = totalCount == 0
            ? 0
            : (int)Math.Ceiling(
                totalCount / (double)pageSize);

        return new PaymentListResponse
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

    // ============================================================
    // NORMALIZATION
    // ============================================================

    private static string NormalizeRequired(
        string value)
    {
        return value.Trim();
    }

    private static string? NormalizeOptional(
        string? value)
    {
        if (string.IsNullOrWhiteSpace(value))
        {
            return null;
        }

        return value.Trim();
    }
}