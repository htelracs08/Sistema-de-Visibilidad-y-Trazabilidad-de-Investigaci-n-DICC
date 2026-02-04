package ec.epn.backend.service.dto;

import java.time.LocalDate;
import java.util.Objects;

public record RegistrarAyudanteCommand(
    String nombres,
    String apellidos,
    String correoInstitucional,
    String facultad,
    int quintil,
    String tipoAyudante,
    LocalDate fechaInicio,
    LocalDate fechaFin
) {
    public RegistrarAyudanteCommand {
        Objects.requireNonNull(nombres, "Nombres no puede ser null");
        Objects.requireNonNull(apellidos, "Apellidos no puede ser null");
        Objects.requireNonNull(correoInstitucional, "Correo institucional no puede ser null");
        Objects.requireNonNull(facultad, "Facultad no puede ser null");
        Objects.requireNonNull(tipoAyudante, "Tipo de ayudante no puede ser null");
        Objects.requireNonNull(fechaInicio, "Fecha de inicio no puede ser null");
        Objects.requireNonNull(fechaFin, "Fecha de fin no puede ser null");

        if (nombres.isBlank()) {
            throw new IllegalArgumentException("Nombres no puede estar vacío");
        }
        if (apellidos.isBlank()) {
            throw new IllegalArgumentException("Apellidos no puede estar vacío");
        }
        if (correoInstitucional.isBlank()) {
            throw new IllegalArgumentException("Correo institucional no puede estar vacío");
        }
        if (quintil < 1 || quintil > 5) {
            throw new IllegalArgumentException("Quintil debe estar entre 1 y 5");
        }
        if (fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException(
                "Fecha de fin no puede ser anterior a fecha de inicio"
            );
        }
    }
}