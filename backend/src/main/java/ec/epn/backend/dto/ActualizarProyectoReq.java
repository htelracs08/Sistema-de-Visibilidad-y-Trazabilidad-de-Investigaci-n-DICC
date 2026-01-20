package ec.epn.backend.dto;

public record ActualizarProyectoReq(
  String fechaInicio,   // YYYY-MM-DD
  String fechaFin,      // YYYY-MM-DD
  String tipo,          // INVESTIGACION | VINCULACION | TRANSFERENCIA_TECNOLOGICA
  String subtipo,       // INTERNO | EXTERNO | SEMILLA | GRUPAL | MULTIDISCIPLINARIO | null
  Integer maxAyudantes,
  Integer maxArticulos
) {}
