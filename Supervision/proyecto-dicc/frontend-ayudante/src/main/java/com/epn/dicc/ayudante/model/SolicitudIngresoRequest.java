package com.epn.dicc.ayudante.model;

import lombok.Data;

/**
 * Request para solicitar ingreso a proyecto
 */
@Data
public class SolicitudIngresoRequest {
    private String codigoProyecto;
    private String comentarios;
}