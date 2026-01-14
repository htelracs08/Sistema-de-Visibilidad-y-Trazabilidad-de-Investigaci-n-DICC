package com.epn.dicc.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad para gestionar capacidad de ayudantes por semestre
 */
@Data
@Entity
@Table(name = "proyecto_capacidad_semestre",
       uniqueConstraints = @UniqueConstraint(columnNames = {"proyecto_id", "numero_semestre"}))
public class ProyectoCapacidadSemestre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @Column(name = "numero_semestre", nullable = false)
    private Integer numeroSemestre;

    @Column(name = "numero_ayudantes", nullable = false)
    private Integer numeroAyudantes;

    @Column(name = "numero_meses_por_ayudante", nullable = false)
    private Integer numeroMesesPorAyudante;
}