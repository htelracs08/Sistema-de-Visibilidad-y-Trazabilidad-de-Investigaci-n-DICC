package com.epn.dicc.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear bitácora
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearBitacoraRequest {
    
    @NotNull(message = "El ID del contrato es obligatorio")
    private Long contratoId;
    
    @NotNull(message = "El mes es obligatorio")
    @Min(value = 1, message = "El mes debe estar entre 1 y 12")
    @Max(value = 12, message = "El mes debe estar entre 1 y 12")
    private Integer mes;
    
    @NotNull(message = "El año es obligatorio")
    private Integer anio;
    
    private String comentariosAyudante;
}