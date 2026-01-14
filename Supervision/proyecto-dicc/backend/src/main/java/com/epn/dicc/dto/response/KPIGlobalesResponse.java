package com.epn.dicc.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para KPIs globales (Jefatura)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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