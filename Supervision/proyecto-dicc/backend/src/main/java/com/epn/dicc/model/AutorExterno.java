package com.epn.dicc.model;

import com.epn.dicc.model.base.EntidadBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Entidad AutorExterno
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "autor_externo")
public class AutorExterno extends EntidadBase {

    @NotBlank(message = "Los nombres son obligatorios")
    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "afiliacion", length = 200)
    private String afiliacion;

    @Column(name = "pais_afiliacion", length = 100)
    private String paisAfiliacion;

    @Column(name = "orcid", length = 50)
    private String orcid;

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }
}