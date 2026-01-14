package com.epn.dicc.ayudante.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Modelo para Bitácora Mensual
 */
@Data
public class BitacoraResponse {
    private Long id;
    private String codigoBitacora;
    private Integer mes;
    private Integer anio;
    private LocalDateTime fechaEnvio;
    private LocalDateTime fechaRevision;
    private String estado;
    private BigDecimal horasTotales;
    private String comentariosAyudante;
    private String comentariosDirector;
    
    private Long contratoId;
    private String numeroContrato;
    
    private List<ActividadResponse> actividades;
    
    private Boolean puedeEditar;
    private Boolean estaEnPlazo;
}