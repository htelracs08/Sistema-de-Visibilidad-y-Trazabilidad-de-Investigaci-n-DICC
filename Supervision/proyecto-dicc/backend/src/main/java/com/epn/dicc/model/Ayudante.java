package com.epn.dicc.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * Entidad Ayudante de Proyecto
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("AYUDANTE")
public class Ayudante extends Usuario {

    @Column(name = "carrera", length = 150)
    private String carrera;

    @Column(name = "facultad", length = 150)
    private String facultad;

    @Min(value = 1, message = "El quintil debe estar entre 1 y 5")
    @Max(value = 5, message = "El quintil debe estar entre 1 y 5")
    @Column(name = "quintil")
    private Integer quintil;

    @Column(name = "semestre_actual")
    private Integer semestreActual;

    @Column(name = "promedio_general", precision = 4, scale = 2)
    private BigDecimal promedioGeneral;
}