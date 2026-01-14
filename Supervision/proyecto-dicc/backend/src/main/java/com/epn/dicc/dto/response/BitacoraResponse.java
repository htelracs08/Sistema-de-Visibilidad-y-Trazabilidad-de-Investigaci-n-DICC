package com.epn.dicc.dto.response;

import com.epn.dicc.model.enums.EstadoBitacora;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta para BitácoraMensual
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BitacoraResponse {
    private Long id;
    private String codigoBitacora;
    private Integer mes;
    private Integer anio;
    private LocalDateTime fechaEnvio;
    private LocalDateTime fechaRevision;
    private EstadoBitacora estado;
    private BigDecimal horasTotales;
    private String comentariosAyudante;
    private String comentariosDirector;
    
    // Información del contrato
    private Long contratoId;
    private String numeroContrato;
    
    // Información del ayudante
    private String nombreAyudante;
    
    // Actividades
    private List<ActividadResponse> actividades;
    
    // Validaciones
    private Boolean puedeEditar;
    private Boolean estaEnPlazo;
}