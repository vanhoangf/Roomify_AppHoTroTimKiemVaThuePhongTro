using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Roomify.API.DTOs.Payments;
using Roomify.API.Helpers;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Controllers;

[ApiController]
[Route("api/student/payments")]
[Authorize(Roles = "STUDENT")]
public class StudentPaymentsController : ControllerBase
{
    private readonly IPaymentService _paymentService;

    public StudentPaymentsController(
        IPaymentService paymentService)
    {
        _paymentService = paymentService;
    }

    // ============================================================
    // GET LIST
    // GET /api/student/payments
    // GET /api/student/payments?status=PENDING
    // GET /api/student/payments?paymentType=RENT
    // ============================================================

    [HttpGet]
    public async Task<ActionResult<PaymentListResponse>> GetPayments(
        [FromQuery] string? status = null,
        [FromQuery] string? paymentType = null,
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 10)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _paymentService.GetStudentPaymentsAsync(
                userId,
                status,
                paymentType,
                page,
                pageSize);

        return Ok(result);
    }

    // ============================================================
    // GET DETAIL
    // GET /api/student/payments/{paymentId}
    // ============================================================

    [HttpGet("{paymentId:long}")]
    public async Task<ActionResult<PaymentResponse>> GetPaymentById(
        long paymentId)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _paymentService.GetStudentPaymentByIdAsync(
                userId,
                paymentId);

        return Ok(result);
    }

    // ============================================================
    // SUBMIT PAYMENT INFORMATION
    // PATCH /api/student/payments/{paymentId}/submit
    // ============================================================

    [HttpPatch("{paymentId:long}/submit")]
    public async Task<ActionResult<PaymentResponse>> SubmitPayment(
        long paymentId,
        [FromBody] SubmitPaymentRequest request)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _paymentService.SubmitAsync(
                userId,
                paymentId,
                request);

        return Ok(result);
    }

    // ============================================================
    // CANCEL
    // PATCH /api/student/payments/{paymentId}/cancel
    // ============================================================

    [HttpPatch("{paymentId:long}/cancel")]
    public async Task<ActionResult<PaymentResponse>> CancelPayment(
        long paymentId)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _paymentService.CancelByStudentAsync(
                userId,
                paymentId);

        return Ok(result);
    }
}