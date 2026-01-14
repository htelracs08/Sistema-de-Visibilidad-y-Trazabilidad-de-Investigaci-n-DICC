package com.epn.dicc.jefatura.model;

import lombok.Data;
import java.time.LocalDate;

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
    private LocalDate fechaFinEstimada;
    private Integer duracionSemestres;
    private Integer semestreActual;
    private Integer totalAyudantesActivos;
    private Integer mesesRestantes;
}