package com.epn.dicc.ayudante.model;

import lombok.Data;
import java.time.LocalDate;

/**
 * Response simple para Proyecto (para ayudante)
 */
@Data
public class ProyectoResponse {
    private Long id;
    private String codigoProyecto;
    private String titulo;
    private String descripcion;
    private String nombreDirector;
    private String nombreLaboratorio;
    private String estado;
    private String tipoProyecto;
    private LocalDate fechaInicioReal;
    private Integer duracionSemestres;
    private Integer semestreActual;
}