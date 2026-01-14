package com.epn.dicc.ayudante.model;

import lombok.Data;

/**
 * Request para crear bitácora
 */
@Data
public class CrearBitacoraRequest {
    private Long contratoId;
    private Integer mes;
    private Integer anio;
    private String comentariosAyudante;
}