package com.epn.dicc.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para agregar actividad a bitácora
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgregarActividadRequest {
    
    @NotNull(message = "El ID de la bitácora es obligatorio")
    private Long bitacoraId;
    
    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;
    
    private String objetivoActividad;
    private String resultadoObtenido;
    
    @NotNull(message = "El tiempo dedicado es obligatorio")
    @Positive(message = "El tiempo debe ser positivo")
    private BigDecimal tiempoDedicadoHoras;
    
    @NotNull(message = "La fecha de ejecución es obligatoria")
    private LocalDate fechaEjecucion;
    
    private String evidenciaUrl;
    private String categoria;
}