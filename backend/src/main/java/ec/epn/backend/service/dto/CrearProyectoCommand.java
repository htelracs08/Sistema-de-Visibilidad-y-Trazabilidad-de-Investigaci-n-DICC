package ec.epn.backend.service.dto;

import ec.epn.backend.domain.TipoProyecto;
import ec.epn.backend.domain.SubtipoProyecto;

import java.util.Objects;

public record CrearProyectoCommand(
    String codigo,
    String nombre,
    String correoDirector,
    TipoProyecto tipo,
    SubtipoProyecto subtipo
) {
    public CrearProyectoCommand {
        Objects.requireNonNull(codigo, "Código no puede ser null");
        Objects.requireNonNull(nombre, "Nombre no puede ser null");
        Objects.requireNonNull(correoDirector, "Correo del director no puede ser null");
        Objects.requireNonNull(tipo, "Tipo de proyecto no puede ser null");

        if (codigo.isBlank()) {
            throw new IllegalArgumentException("Código no puede estar vacío");
        }
        if (nombre.isBlank()) {
            throw new IllegalArgumentException("Nombre no puede estar vacío");
        }
        if (correoDirector.isBlank()) {
            throw new IllegalArgumentException("Correo del director no puede estar vacío");
        }

        // Validación de negocio: subtipo solo para INVESTIGACION
        if (tipo != TipoProyecto.INVESTIGACION && subtipo != null) {
            throw new IllegalArgumentException(
                "Subtipo solo es válido para proyectos de INVESTIGACION"
            );
        }
    }

    public static CrearProyectoCommand fromRequest(
        String codigo,
        String nombre,
        String correoDirector,
        String tipoStr,
        String subtipoStr
    ) {
        TipoProyecto tipo = tipoStr != null 
            ? TipoProyecto.fromString(tipoStr) 
            : null;
        
        SubtipoProyecto subtipo = (tipo == TipoProyecto.INVESTIGACION && subtipoStr != null)
            ? SubtipoProyecto.fromString(subtipoStr)
            : null;

        return new CrearProyectoCommand(
            codigo != null ? codigo.trim() : null,
            nombre != null ? nombre.trim() : null,
            correoDirector != null ? correoDirector.trim().toLowerCase() : null,
            tipo,
            subtipo
        );
    }
}