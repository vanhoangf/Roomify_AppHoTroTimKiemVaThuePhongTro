
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Roomify.API.DTOs.Student;
using Roomify.API.Helpers;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Controllers;

[ApiController]
[Route("api/student")]
[Authorize(Roles = "STUDENT")]
public class StudentViewingAppointmentsController : ControllerBase
{
    private readonly IViewingAppointmentService _service;

    public StudentViewingAppointmentsController(
        IViewingAppointmentService service)
    {
        _service = service;
    }

    // POST - Dat lich xem phong
    [HttpPost("rooms/{roomId:long}/viewing-appointments")]
    public async Task<IActionResult> Create(
        long roomId,
        [FromBody] CreateViewingAppointmentRequest request,
        CancellationToken ct)
    {
        var result = await _service.CreateAsync(
            User.GetRequiredUserId(), roomId, request, ct);

        return StatusCode(
            StatusCodes.Status201Created, result);
    }

    // GET - Danh sach lich hen cua sinh vien
    [HttpGet("viewing-appointments")]
    public async Task<IActionResult> GetList(
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 10,
        CancellationToken ct = default)
    {
        return Ok(await _service.GetListAsync(
            User.GetRequiredUserId(),
            false, page, pageSize, ct));
    }

    // GET - Chi tiet lich hen
    [HttpGet("viewing-appointments/{appointmentId:long}")]
    public async Task<IActionResult> Detail(
        long appointmentId,
        CancellationToken ct)
    {
        return Ok(await _service.GetDetailAsync(
            User.GetRequiredUserId(),
            appointmentId, false, ct));
    }

    // GET - Lich su lich hen
    [HttpGet("viewing-appointments/{appointmentId:long}/history")]
    public async Task<IActionResult> History(
        long appointmentId,
        CancellationToken ct)
    {
        return Ok(await _service.GetHistoryAsync(
            User.GetRequiredUserId(),
            appointmentId, false, ct));
    }

    // PATCH - Huy lich
    [HttpPatch("viewing-appointments/{appointmentId:long}/cancel")]
    public async Task<IActionResult> Cancel(
        long appointmentId,
        CancellationToken ct)
    {
        return Ok(await _service.UpdateAsync(
            User.GetRequiredUserId(),
            appointmentId,
            false, "CANCEL", null, null, ct));
    }

    // PATCH - Chap nhan gio chu tro de xuat
    [HttpPatch("viewing-appointments/{appointmentId:long}/accept-proposal")]
    public async Task<IActionResult> AcceptProposal(
        long appointmentId,
        CancellationToken ct)
    {
        return Ok(await _service.UpdateAsync(
            User.GetRequiredUserId(),
            appointmentId,
            false, "ACCEPT_PROPOSAL", null, null, ct));
    }
}
