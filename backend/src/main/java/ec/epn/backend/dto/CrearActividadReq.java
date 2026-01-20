package ec.epn.backend.dto;

public record CrearActividadReq(
  String horaInicio,   // "HH:mm"
  String horaSalida,   // "HH:mm"
  String descripcion
) {}
