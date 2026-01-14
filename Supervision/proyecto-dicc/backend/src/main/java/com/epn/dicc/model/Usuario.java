package com.epn.dicc.model;

import com.epn.dicc.model.base.EntidadBase;
import com.epn.dicc.model.enums.Rol;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Entidad base Usuario (superclase con herencia SINGLE_TABLE)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_usuario", discriminatorType = DiscriminatorType.STRING)
public abstract class Usuario extends EntidadBase {

    @NotBlank(message = "El código EPN es obligatorio")
    @Column(name = "codigo_epn", unique = true, nullable = false, length = 50)
    private String codigoEPN;

    @NotBlank(message = "La cédula es obligatoria")
    @Size(min = 10, max = 10, message = "La cédula debe tener 10 dígitos")
    @Column(name = "cedula", unique = true, nullable = false, length = 10)
    private String cedula;

    @NotBlank(message = "Los nombres son obligatorios")
    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @NotBlank(message = "El correo institucional es obligatorio")
    @Email(message = "El correo debe ser válido")
    @Column(name = "correo_institucional", unique = true, nullable = false, length = 150)
    private String correoInstitucional;

    @NotBlank(message = "La contraseña es obligatoria")
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 30)
    private Rol rol;

    @Column(name = "email_verificado")
    private Boolean emailVerificado = false;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    // Métodos de utilidad
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    public boolean esCorreoInstitucional() {
        return correoInstitucional != null && 
               correoInstitucional.toLowerCase().endsWith("@epn.edu.ec");
    }

    @PrePersist
    protected void onCreate() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
    }
}