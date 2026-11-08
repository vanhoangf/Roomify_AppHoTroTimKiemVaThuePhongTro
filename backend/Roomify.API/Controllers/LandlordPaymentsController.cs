using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Roomify.API.DTOs.Payments;
using Roomify.API.Helpers;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Controllers;

[ApiController]
[Route("api/landlord")]
[Authorize(Roles = "LANDLORD")]
public class LandlordPaymentsController : ControllerBase
{
    private readonly IPaymentService _paymentService;

    public LandlordPaymentsController(
        IPaymentService paymentService)
    {
        _paymentService = paymentService;
    }

    // ============================================================
    // CREATE PAYMENT
    // POST /api/landlord/contracts/{contractId}/payments
    // ============================================================

    [HttpPost("contracts/{contractId:long}/payments")]
    public async Task<ActionResult<PaymentResponse>> CreatePayment(
        long contractId,
        [FromBody] CreatePaymentRequest request)
    {
        var userId = User.GetRequiredUserId();

        var result = await _paymentService.CreateAsync(
            userId,
            contractId,
            request);

        return CreatedAtAction(
            nameof(GetPaymentById),
            new { paymentId = result.PaymentId },
            result);
    }

    // ============================================================
    // GET LIST
    // GET /api/landlord/payments
    // GET /api/landlord/payments?status=PENDING
    // GET /api/landlord/payments?paymentType=RENT
    // ============================================================

    [HttpGet("payments")]
    public async Task<ActionResult<PaymentListResponse>> GetPayments(
        [FromQuery] string? status = null,
        [FromQuery] string? paymentType = null,
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 10)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _paymentService.GetLandlordPaymentsAsync(
                userId,
                status,
                paymentType,
                page,
                pageSize);

        return Ok(result);
    }

    // ============================================================
    // GET DETAIL
    // GET /api/landlord/payments/{paymentId}
    // ============================================================

    [HttpGet("payments/{paymentId:long}")]
    public async Task<ActionResult<PaymentResponse>> GetPaymentById(
        long paymentId)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _paymentService.GetLandlordPaymentByIdAsync(
                userId,
                paymentId);

        return Ok(result);
    }

    // ============================================================
    // CONFIRM PAYMENT
    // PATCH /api/landlord/payments/{paymentId}/confirm
    // ============================================================

    [HttpPatch("payments/{paymentId:long}/confirm")]
    public async Task<ActionResult<PaymentResponse>> ConfirmPayment(
        long paymentId,
        [FromBody] ConfirmPaymentRequest request)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _paymentService.ConfirmAsync(
                userId,
                paymentId,
                request);

        return Ok(result);
    }

    // ============================================================
    // REJECT PAYMENT
    // PATCH /api/landlord/payments/{paymentId}/reject
    // ============================================================

    [HttpPatch("payments/{paymentId:long}/reject")]
    public async Task<ActionResult<PaymentResponse>> RejectPayment(
        long paymentId,
        [FromBody] RejectPaymentRequest request)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _paymentService.RejectAsync(
                userId,
                paymentId,
                request);

        return Ok(result);
    }

    // ============================================================
    // CANCEL PAYMENT
    // PATCH /api/landlord/payments/{paymentId}/cancel
    // ============================================================

    [HttpPatch("payments/{paymentId:long}/cancel")]
    public async Task<ActionResult<PaymentResponse>> CancelPayment(
        long paymentId)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _paymentService.CancelByLandlordAsync(
                userId,
                paymentId);

        return Ok(result);
    }
}