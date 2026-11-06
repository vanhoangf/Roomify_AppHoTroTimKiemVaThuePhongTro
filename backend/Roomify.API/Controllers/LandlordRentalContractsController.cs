using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Roomify.API.DTOs.Contracts;
using Roomify.API.Helpers;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Controllers;

[ApiController]
[Route("api/landlord")]
[Authorize(Roles = "LANDLORD")]
public class LandlordRentalContractsController : ControllerBase
{
    private readonly IRentalContractService _rentalContractService;

    public LandlordRentalContractsController(
        IRentalContractService rentalContractService)
    {
        _rentalContractService = rentalContractService;
    }

    // ============================================================
    // POST: /api/landlord/rental-requests/{rentalRequestId}/contract
    // Tạo hợp đồng từ RentalRequest đã APPROVED
    // ============================================================
    [HttpPost("rental-requests/{rentalRequestId:long}/contract")]
    public async Task<ActionResult<RentalContractResponse>> CreateContract(
        long rentalRequestId,
        [FromBody] CreateRentalContractRequest request)
    {
        var userId = User.GetRequiredUserId();

        var result = await _rentalContractService.CreateAsync(
            userId,
            rentalRequestId,
            request);

        return StatusCode(StatusCodes.Status201Created, result);
    }

    // ============================================================
    // GET: /api/landlord/contracts
    // ============================================================
    [HttpGet("contracts")]
    public async Task<ActionResult<RentalContractListResponse>> GetContracts(
        [FromQuery] string? status = null,
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 10)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _rentalContractService.GetLandlordContractsAsync(
                userId,
                status,
                page,
                pageSize);

        return Ok(result);
    }

    // ============================================================
    // GET: /api/landlord/contracts/{contractId}
    // ============================================================
    [HttpGet("contracts/{contractId:long}")]
    public async Task<ActionResult<RentalContractResponse>> GetContractById(
        long contractId)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _rentalContractService.GetLandlordContractByIdAsync(
                userId,
                contractId);

        return Ok(result);
    }

    // ============================================================
    // PATCH: /api/landlord/contracts/{contractId}/complete
    // ACTIVE -> COMPLETED
    // ============================================================
    [HttpPatch("contracts/{contractId:long}/complete")]
    public async Task<ActionResult<RentalContractResponse>> CompleteContract(
        long contractId)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _rentalContractService.CompleteAsync(
                userId,
                contractId);

        return Ok(result);
    }

    // ============================================================
    // PATCH: /api/landlord/contracts/{contractId}/cancel
    // ============================================================
    [HttpPatch("contracts/{contractId:long}/cancel")]
    public async Task<ActionResult<RentalContractResponse>> CancelContract(
        long contractId)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _rentalContractService.CancelByLandlordAsync(
                userId,
                contractId);

        return Ok(result);
    }
}