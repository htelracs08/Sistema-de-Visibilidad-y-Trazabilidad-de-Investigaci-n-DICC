package com.epn.dicc.model;

import com.epn.dicc.model.base.EntidadBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * Entidad Artículo
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "articulo")
public class Articulo extends EntidadBase {

    @NotBlank(message = "El DOI es obligatorio")
    @Column(name = "doi", unique = true, nullable = false, length = 200)
    private String doi;

    @NotBlank(message = "El título es obligatorio")
    @Column(name = "titulo", nullable = false, length = 500)
    private String titulo;

    @Column(name = "revista", length = 200)
    private String revista;

    @Column(name = "volumen", length = 50)
    private String volumen;

    @Column(name = "numero", length = 50)
    private String numero;

    @Column(name = "pagina_inicio")
    private Integer paginaInicio;

    @Column(name = "pagina_fin")
    private Integer paginaFin;

    @Column(name = "fecha_publicacion")
    private LocalDate fechaPublicacion;

    @Column(name = "cuartil", length = 10)
    private String cuartil; // Q1, Q2, Q3, Q4

    @Column(name = "indexacion", length = 100)
    private String indexacion; // Scopus, WoS, etc.

    @Column(name = "url_acceso", length = 500)
    private String urlAcceso;

    @Column(name = "estado_publicacion", length = 50)
    private String estadoPublicacion; // En prensa, Publicado, etc.

    // Relación
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;
}