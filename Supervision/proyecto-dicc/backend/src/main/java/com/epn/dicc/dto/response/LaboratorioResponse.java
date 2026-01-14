package com.epn.dicc.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para Laboratorio
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LaboratorioResponse {
    private Long id;
    private String codigoLaboratorio;
    private String nombre;
    private String ubicacion;
    private String responsable;
    private String extension;
    private Integer totalProyectosActivos;
}