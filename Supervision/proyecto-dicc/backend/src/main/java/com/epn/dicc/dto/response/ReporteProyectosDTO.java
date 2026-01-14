package com.epn.dicc.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteProyectosDTO {
    private Integer totalProyectos;
    private Integer proyectosActivos;
    private Integer proyectosFinalizados;
}