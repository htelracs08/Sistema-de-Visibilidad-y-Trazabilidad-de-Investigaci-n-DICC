package com.epn.dicc.dto.request;

import com.epn.dicc.model.enums.Rol;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para registro de usuario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroUsuarioRequest {
    
    @NotBlank(message = "El código EPN es obligatorio")
    private String codigoEPN;
    
    @NotBlank(message = "La cédula es obligatoria")
    @Size(min = 10, max = 10, message = "La cédula debe tener 10 dígitos")
    private String cedula;
    
    @NotBlank(message = "Los nombres son obligatorios")
    private String nombres;
    
    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidos;
    
    @NotBlank(message = "El correo institucional es obligatorio")
    @Email(message = "Debe ser un correo válido")
    @Pattern(regexp = ".*@epn\\.edu\\.ec$", message = "Debe ser un correo institucional @epn.edu.ec")
    private String correoInstitucional;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;
    
    @NotNull(message = "El rol es obligatorio")
    private Rol rol;
    
    // Código especial solo para Jefatura
    private String codigoEspecialJefatura;
    
    // Campos específicos de Docente
    private String departamento;
    private String cubiculo;
    private String extension;
    private String areaInvestigacion;
    
    // Campos específicos de Ayudante
    private String carrera;
    private String facultad;
    
    @Min(value = 1, message = "El quintil debe estar entre 1 y 5")
    @Max(value = 5, message = "El quintil debe estar entre 1 y 5")
    private Integer quintil;
    
    private Integer semestreActual;
    private BigDecimal promedioGeneral;
}