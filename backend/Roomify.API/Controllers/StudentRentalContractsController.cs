using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Roomify.API.DTOs.Contracts;
using Roomify.API.Helpers;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Controllers;

[ApiController]
[Route("api/student/contracts")]
[Authorize(Roles = "STUDENT")]
public class StudentRentalContractsController : ControllerBase
{
    private readonly IRentalContractService _rentalContractService;

    public StudentRentalContractsController(
        IRentalContractService rentalContractService)
    {
        _rentalContractService = rentalContractService;
    }

    // ============================================================
    // GET: /api/student/contracts
    // ============================================================
    [HttpGet]
    public async Task<ActionResult<RentalContractListResponse>> GetContracts(
        [FromQuery] string? status = null,
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 10)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _rentalContractService.GetStudentContractsAsync(
                userId,
                status,
                page,
                pageSize);

        return Ok(result);
    }

    // ============================================================
    // GET: /api/student/contracts/{contractId}
    // ============================================================
    [HttpGet("{contractId:long}")]
    public async Task<ActionResult<RentalContractResponse>> GetContractById(
        long contractId)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _rentalContractService.GetStudentContractByIdAsync(
                userId,
                contractId);

        return Ok(result);
    }

    // ============================================================
    // PATCH: /api/student/contracts/{contractId}/activate
    // PENDING -> ACTIVE
    // ============================================================
    [HttpPatch("{contractId:long}/activate")]
    public async Task<ActionResult<RentalContractResponse>> ActivateContract(
        long contractId)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _rentalContractService.ActivateAsync(
                userId,
                contractId);

        return Ok(result);
    }

    // ============================================================
    // PATCH: /api/student/contracts/{contractId}/cancel
    // ============================================================
    [HttpPatch("{contractId:long}/cancel")]
    public async Task<ActionResult<RentalContractResponse>> CancelContract(
        long contractId)
    {
        var userId = User.GetRequiredUserId();

        var result =
            await _rentalContractService.CancelByStudentAsync(
                userId,
                contractId);

        return Ok(result);
    }
}