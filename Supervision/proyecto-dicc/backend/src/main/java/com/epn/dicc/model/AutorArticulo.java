package com.epn.dicc.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad AutorArticulo (relación N:M entre Articulo y Autores)
 */
@Data
@Entity
@Table(name = "autor_articulo")
public class AutorArticulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_id", nullable = false)
    private Articulo articulo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuarioInterno; // Puede ser Docente o Ayudante

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_externo_id")
    private AutorExterno autorExterno;

    @Column(name = "orden_autoria", nullable = false)
    private Integer ordenAutoria;

    @Column(name = "es_autor_correspondiente")
    private Boolean esAutorCorrespondiente = false;

    @Column(name = "contribucion", columnDefinition = "TEXT")
    private String contribucion;
}
