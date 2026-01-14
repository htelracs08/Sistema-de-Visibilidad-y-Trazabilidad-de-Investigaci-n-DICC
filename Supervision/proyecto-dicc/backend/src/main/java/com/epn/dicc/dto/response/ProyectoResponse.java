package com.epn.dicc.dto.response;

import com.epn.dicc.model.enums.EstadoProyecto;
import com.epn.dicc.model.enums.TipoProyecto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO de respuesta para Proyecto
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProyectoResponse {
    private Long id;
    private String codigoProyecto;
    private String titulo;
    private String descripcion;
    private LocalDate fechaInicioReal;
    private LocalDate fechaFinEstimada;
    private Integer duracionSemestres;
    private Integer semestreActual;
    private EstadoProyecto estado;
    private TipoProyecto tipoProyecto;
    
    // Información del director
    private Long directorId;
    private String nombreDirector;
    
    // Información del laboratorio
    private Long laboratorioId;
    private String nombreLaboratorio;
    
    // Estadísticas
    private Integer totalAyudantesActivos;
    private Integer totalAyudantesHistoricos;
    private Integer mesesRestantes;
}