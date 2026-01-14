package com.epn.dicc.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para aprobar contrato
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AprobacionContratoRequest {
    
    @NotNull(message = "El ID del contrato es obligatorio")
    private Long contratoId;
    
    @NotNull(message = "Los meses asignados son obligatorios")
    @Min(value = 1, message = "Debe ser al menos 1 mes")
    @Max(value = 6, message = "No puede ser más de 6 meses")
    private Integer mesesAsignados;
    
    @NotNull(message = "El semestre es obligatorio")
    private Integer semestreAsignado;
    
    @NotNull(message = "Las horas semanales son obligatorias")
    @Min(value = 1, message = "Debe ser al menos 1 hora")
    @Max(value = 40, message = "No puede ser más de 40 horas")
    private Integer horasSemanales;
    
    @NotNull(message = "La remuneración es obligatoria")
    @Positive(message = "La remuneración debe ser positiva")
    private BigDecimal remuneracionMensual;
    
    private String comentarios;
}