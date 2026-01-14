package com.epn.dicc.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para registrar renuncia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrarRenunciaRequest {
    
    @NotNull(message = "El ID del contrato es obligatorio")
    private Long contratoId;
    
    private LocalDate fechaRenuncia;
    
    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;
}