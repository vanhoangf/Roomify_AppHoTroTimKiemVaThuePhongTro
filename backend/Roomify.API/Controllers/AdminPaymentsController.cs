using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

using Roomify.API.DTOs.Admin.Payments;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Controllers;

[ApiController]
[Route("api/admin/payments")]
[Authorize(Roles = "ADMIN")]
public class AdminPaymentsController
    : ControllerBase
{
    private readonly IAdminPaymentService
        _adminPaymentService;


    public AdminPaymentsController(
        IAdminPaymentService adminPaymentService)
    {
        _adminPaymentService =
            adminPaymentService;
    }


    // ============================================================
    // GET LIST
    // GET /api/admin/payments
    // ============================================================

    [HttpGet]
    public async Task<ActionResult<AdminPaymentListResponse>>
        GetPayments(
            [FromQuery] string? status = null,
            [FromQuery] string? paymentType = null,
            [FromQuery] string? search = null,
            [FromQuery] int page = 1,
            [FromQuery] int pageSize = 10)
    {
        var result =
            await _adminPaymentService
                .GetPaymentsAsync(
                    status,
                    paymentType,
                    search,
                    page,
                    pageSize);


        return Ok(
            result);
    }


    // ============================================================
    // GET DETAIL
    // GET /api/admin/payments/{paymentId}
    // ============================================================

    [HttpGet("{paymentId:long}")]
    public async Task<ActionResult<AdminPaymentResponse>>
        GetPaymentById(
            long paymentId)
    {
        var result =
            await _adminPaymentService
                .GetPaymentByIdAsync(
                    paymentId);


        return Ok(
            result);
    }
}