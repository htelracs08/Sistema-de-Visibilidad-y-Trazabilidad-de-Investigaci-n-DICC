package com.epn.dicc.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de respuesta para Actividad
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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