package com.epn.dicc.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitud de ingreso a proyecto
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudIngresoRequest {
    
    @NotBlank(message = "El código del proyecto es obligatorio")
    private String codigoProyecto;
    
    private String comentarios;
}