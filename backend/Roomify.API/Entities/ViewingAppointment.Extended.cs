namespace Roomify.API.Entities;

public partial class ViewingAppointment
{
    public DateTime? ProposedAt { get; set; }

    public byte[] RowVer { get; set; } = [];
}