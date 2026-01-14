package com.epn.dicc.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para rechazar contrato
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RechazoContratoRequest {
    
    @NotNull(message = "El ID del contrato es obligatorio")
    private Long contratoId;
    
    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;
}