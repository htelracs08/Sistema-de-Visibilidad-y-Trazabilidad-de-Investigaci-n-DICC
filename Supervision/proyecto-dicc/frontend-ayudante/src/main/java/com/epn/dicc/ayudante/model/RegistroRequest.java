package com.epn.dicc.ayudante.model;

import lombok.Data;
import java.math.BigDecimal;

/**
 * Request para registro de ayudante
 */
@Data
public class RegistroRequest {
    private String codigoEPN;
    private String cedula;
    private String nombres;
    private String apellidos;
    private String correoInstitucional;
    private String password;
    private String rol = "AYUDANTE_PROYECTO";
    
    // Campos específicos de ayudante
    private String carrera;
    private String facultad;
    private Integer quintil;
    private Integer semestreActual;
    private BigDecimal promedioGeneral;
}