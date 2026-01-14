package com.epn.dicc.ayudante.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request para agregar actividad
 */
@Data
public class AgregarActividadRequest {
    private Long bitacoraId;
    private String descripcion;
    private String objetivoActividad;
    private String resultadoObtenido;
    private BigDecimal tiempoDedicadoHoras;
    private LocalDate fechaEjecucion;
    private String evidenciaUrl;
    private String categoria;
}