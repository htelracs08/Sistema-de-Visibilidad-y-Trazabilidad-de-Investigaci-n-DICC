package com.epn.dicc.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Entidad Docente (Director de Proyecto)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("DOCENTE")
public class Docente extends Usuario {

    @Column(name = "departamento", length = 100)
    private String departamento;

    @Column(name = "cubiculo", length = 50)
    private String cubiculo;

    @Column(name = "extension", length = 20)
    private String extension;

    @Column(name = "area_investigacion", length = 200)
    private String areaInvestigacion;
}