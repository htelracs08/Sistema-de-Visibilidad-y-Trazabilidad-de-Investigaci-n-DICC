package com.epn.dicc.dto.response;

import com.epn.dicc.model.enums.EstadoContrato;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para Contrato
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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
    private EstadoContrato estado;
    private Integer semestreAsignado;
    
    // Información del proyecto
    private Long proyectoId;
    private String codigoProyecto;
    private String tituloProyecto;
    
    // Información del ayudante
    private Long ayudanteId;
    private String nombreAyudante;
    private String codigoEPNAyudante;
    
    // Información del director
    private String nombreDirector;
    
    // Cálculos
    private Integer mesesRestantes;
    private Boolean puedeReemplazarse;
}