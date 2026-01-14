package com.epn.dicc.ayudante.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Response de usuario
 */
@Data
public class UsuarioResponse {
    private Long id;
    private String codigoEPN;
    private String nombres;
    private String apellidos;
    private String nombreCompleto;
    private String correoInstitucional;
    private String rol;
    private String carrera;
    private String facultad;
    private Integer quintil;
    private LocalDateTime fechaRegistro;
}