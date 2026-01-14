package com.epn.dicc.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para estadísticas de un proyecto
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasProyectoResponse {
    private Long proyectoId;
    private String codigoProyecto;
    private String titulo;
    
    // Ayudantes
    private Integer totalAyudantesActuales;
    private Integer totalAyudantesHistoricos;
    private Integer totalSolicitudesPendientes;
    
    // Bitácoras
    private Integer totalBitacorasAprobadas;
    private Integer totalBitacorasPendientes;
    private Double horasTotalesTrabajadas;
    
    // Producción científica
    private Integer totalArticulosPublicados;
    private Integer totalArticulosEnProceso;
    
    // Tiempo
    private Integer mesesTranscurridos;
    private Integer mesesRestantes;
}