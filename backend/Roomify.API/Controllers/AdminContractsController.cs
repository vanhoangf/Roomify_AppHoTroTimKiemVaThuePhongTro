using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

using Roomify.API.DTOs.Admin.Contracts;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Controllers;

[ApiController]
[Route("api/admin/contracts")]
[Authorize(Roles = "ADMIN")]
public class AdminContractsController
    : ControllerBase
{
    private readonly IAdminContractService
        _adminContractService;


    public AdminContractsController(
        IAdminContractService adminContractService)
    {
        _adminContractService =
            adminContractService;
    }


    // ============================================================
    // GET LIST
    // GET /api/admin/contracts
    // ============================================================

    [HttpGet]
    public async Task<ActionResult<AdminContractListResponse>>
        GetContracts(
            [FromQuery] string? status = null,
            [FromQuery] string? search = null,
            [FromQuery] int page = 1,
            [FromQuery] int pageSize = 10)
    {
        var result =
            await _adminContractService
                .GetContractsAsync(
                    status,
                    search,
                    page,
                    pageSize);


        return Ok(
            result);
    }


    // ============================================================
    // GET DETAIL
    // GET /api/admin/contracts/{contractId}
    // ============================================================

    [HttpGet("{contractId:long}")]
    public async Task<ActionResult<AdminContractResponse>>
        GetContractById(
            long contractId)
    {
        var result =
            await _adminContractService
                .GetContractByIdAsync(
                    contractId);


        return Ok(
            result);
    }
}