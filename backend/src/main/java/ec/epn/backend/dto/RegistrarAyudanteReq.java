package ec.epn.backend.dto;

public record RegistrarAyudanteReq(
  String nombres,
  String apellidos,
  String correoInstitucional,
  String facultad,
  Integer quintil,
  String tipoAyudante,
  String fechaInicioContrato,
  String fechaFinContrato
) {}