package ec.epn.backend.service.dto;

public record RegistrarAyudanteResult(
    String ayudanteId,
    String contratoId,
    boolean ayudanteCreado,
    boolean usuarioCreado
) {}