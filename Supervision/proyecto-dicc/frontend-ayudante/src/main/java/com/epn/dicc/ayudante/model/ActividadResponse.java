package com.epn.dicc.ayudante.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Modelo para Actividad
 */
@Data
public class ActividadResponse {
    private Long id;
    private Integer numeroActividad;
    private String descripcion;
    private String objetivoActividad;
    private String resultadoObtenido;
    private BigDecimal tiempoDedicadoHoras;
    private LocalDate fechaEjecucion;
    private String evidenciaUrl;
    private String categoria;
}