package ec.epn.backend.service.dto;

public record CrearProyectoResult(
    String proyectoId,
    boolean directorCreado,
    boolean notificacionEnviada
) {}