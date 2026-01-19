package ec.epn.backend.dto;

public record CrearProyectoReq(
  String codigo,
  String nombre,
  String correoDirector
) {}
