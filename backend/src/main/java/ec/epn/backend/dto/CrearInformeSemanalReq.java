package ec.epn.backend.dto;

public record CrearInformeSemanalReq(
  String fechaInicioSemana,
  String fechaFinSemana,
  String actividadesRealizadas,
  String observaciones,
  String anexos
) {}
