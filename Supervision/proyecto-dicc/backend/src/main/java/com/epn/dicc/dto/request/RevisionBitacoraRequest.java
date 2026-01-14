package com.epn.dicc.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para revisar bitácora (aprobar/rechazar)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevisionBitacoraRequest {
    
    @NotNull(message = "El ID de la bitácora es obligatorio")
    private Long bitacoraId;
    
    @NotBlank(message = "La acción es obligatoria (APROBAR/RECHAZAR/MODIFICAR)")
    private String accion; // APROBAR, RECHAZAR, MODIFICAR
    
    private String comentariosDirector;
}