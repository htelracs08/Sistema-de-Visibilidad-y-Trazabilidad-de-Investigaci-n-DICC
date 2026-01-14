package com.epn.dicc.model;

import com.epn.dicc.model.base.EntidadBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Entidad Laboratorio
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "laboratorio")
public class Laboratorio extends EntidadBase {

    @NotBlank(message = "El código del laboratorio es obligatorio")
    @Column(name = "codigo_laboratorio", unique = true, nullable = false, length = 50)
    private String codigoLaboratorio;

    @NotBlank(message = "El nombre del laboratorio es obligatorio")
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "ubicacion", length = 200)
    private String ubicacion;

    @Column(name = "responsable", length = 200)
    private String responsable;

    @Column(name = "extension", length = 20)
    private String extension;
}