package com.epn.dicc.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para relación Artículo-Ayudante (para Jefatura)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelacionArticuloAyudanteResponse {
    private String doiArticulo;
    private String tituloArticulo;
    private String codigoProyecto;
    private String tituloProyecto;
    private String nombreAyudante;
    private String codigoEPNAyudante;
    private Integer ordenAutoria;
    private Boolean esAutorCorrespondiente;
}