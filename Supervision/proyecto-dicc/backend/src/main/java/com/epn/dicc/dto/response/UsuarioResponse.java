package com.epn.dicc.dto.response;

import com.epn.dicc.model.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para información de usuario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {
    private Long id;
    private String codigoEPN;
    private String cedula;
    private String nombres;
    private String apellidos;
    private String nombreCompleto;
    private String correoInstitucional;
    private Rol rol;
    private Boolean emailVerificado;
    private LocalDateTime fechaRegistro;
    
    // Campos específicos según tipo
    private String departamento; // Docente
    private String carrera; // Ayudante
    private Integer quintil; // Ayudante
}