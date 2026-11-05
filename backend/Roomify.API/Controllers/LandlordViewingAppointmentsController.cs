
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Roomify.API.DTOs.Student;
using Roomify.API.Helpers;
using Roomify.API.Services.Interfaces;

namespace Roomify.API.Controllers;

[ApiController]
[Route("api/landlord/viewing-appointments")]
[Authorize(Roles = "LANDLORD")]
public class LandlordViewingAppointmentsController : ControllerBase
{
    private readonly IViewingAppointmentService _service;

    public LandlordViewingAppointmentsController(
        IViewingAppointmentService service)
    {
        _service = service;
    }

    // GET - Danh sach lich hen
    [HttpGet]
    public async Task<IActionResult> GetList(
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 10,
        CancellationToken ct = default)
    {
        return Ok(await _service.GetListAsync(
            User.GetRequiredUserId(),
            true, page, pageSize, ct));
    }

    // GET - Chi tiet lich hen
    [HttpGet("{appointmentId:long}")]
    public async Task<IActionResult> Detail(
        long appointmentId,
        CancellationToken ct)
    {
        return Ok(await _service.GetDetailAsync(
            User.GetRequiredUserId(),
            appointmentId, true, ct));
    }

    // GET - Lich su xu ly
    [HttpGet("{appointmentId:long}/history")]
    public async Task<IActionResult> History(
        long appointmentId,
        CancellationToken ct)
    {
        return Ok(await _service.GetHistoryAsync(
            User.GetRequiredUserId(),
            appointmentId, true, ct));
    }

    // PATCH - Xac nhan lich
    [HttpPatch("{appointmentId:long}/confirm")]
    public async Task<IActionResult> Confirm(
        long appointmentId,
        [FromBody] ViewingAppointmentNoteRequest? request,
        CancellationToken ct)
    {
        return Ok(await _service.UpdateAsync(
            User.GetRequiredUserId(),
            appointmentId, true,
            "CONFIRM", null, request?.Note, ct));
    }

    // PATCH - Tu choi lich
    [HttpPatch("{appointmentId:long}/reject")]
    public async Task<IActionResult> Reject(
        long appointmentId,
        [FromBody] ViewingAppointmentNoteRequest request,
        CancellationToken ct)
    {
        return Ok(await _service.UpdateAsync(
            User.GetRequiredUserId(),
            appointmentId, true,
            "REJECT", null, request.Note, ct));
    }

    // PATCH - De xuat thoi gian moi
    [HttpPatch("{appointmentId:long}/propose")]
    public async Task<IActionResult> Propose(
        long appointmentId,
        [FromBody] ProposeViewingTimeRequest request,
        CancellationToken ct)
    {
        return Ok(await _service.UpdateAsync(
            User.GetRequiredUserId(),
            appointmentId, true,
            "PROPOSE", request.ProposedAt, request.Note, ct));
    }

    // PATCH - Danh dau da xem phong
    [HttpPatch("{appointmentId:long}/complete")]
    public async Task<IActionResult> Complete(
        long appointmentId,
        [FromBody] ViewingAppointmentNoteRequest? request,
        CancellationToken ct)
    {
        return Ok(await _service.UpdateAsync(
            User.GetRequiredUserId(),
            appointmentId, true,
            "COMPLETE", null, request?.Note, ct));
    }
}
