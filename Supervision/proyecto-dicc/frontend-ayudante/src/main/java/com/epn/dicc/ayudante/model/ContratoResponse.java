package com.epn.dicc.ayudante.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modelo para Contrato
 */
@Data
public class ContratoResponse {
    private Long id;
    private String numeroContrato;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaAprobacion;
    private LocalDate fechaInicioContrato;
    private LocalDate fechaFinContrato;
    private Integer mesesPactados;
    private Integer mesesTrabajados;
    private Integer horasSemanalesPactadas;
    private BigDecimal remuneracionMensual;
    private String estado;
    private Integer semestreAsignado;
    
    // Proyecto
    private Long proyectoId;
    private String codigoProyecto;
    private String tituloProyecto;
    
    // Director
    private String nombreDirector;
    
    // Cálculos
    private Integer mesesRestantes;
}