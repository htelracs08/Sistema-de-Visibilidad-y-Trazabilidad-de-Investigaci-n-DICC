package com.epn.dicc.jefatura.model;

import lombok.Data;

@Data
public class KPIGlobalesResponse {
    private Integer totalProyectosActivos;
    private Integer totalProyectosFinalizados;
    private Integer totalProyectosSuspendidos;
    private Integer totalAyudantesActivos;
    private Integer totalAyudantesHistoricos;
    private Integer totalDirectores;
    private Integer totalArticulosPublicados;
    private Integer totalLaboratorios;
}