package ec.epn.backend.domain;

public record Ayudante(
  String id,
  String nombres,
  String apellidos,
  String correoInstitucional,
  String facultad,
  int quintil,
  String tipoAyudante
) {}