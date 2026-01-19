package ec.epn.backend.domain;

public record Contrato(
  String id,
  String proyectoId,
  String ayudanteId,
  String fechaInicio,
  String fechaFin,
  String estado,
  String motivoInactivo
) {}