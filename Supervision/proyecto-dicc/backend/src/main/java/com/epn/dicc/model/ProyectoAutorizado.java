package com.epn.dicc.model;

import com.epn.dicc.model.base.EntidadBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Entidad ProyectoAutorizado
 * Códigos de proyectos autorizados por Jefatura
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "proyecto_autorizado")
public class ProyectoAutorizado extends EntidadBase {

    @NotBlank(message = "El código del proyecto es obligatorio")
    @Column(name = "codigo_proyecto", unique = true, nullable = false, length = 50)
    private String codigoProyecto;

    @Column(name = "fecha_autorizacion")
    private LocalDateTime fechaAutorizacion;

    @Column(name = "autorizado_por", length = 100)
    private String autorizadoPor;

    @Column(name = "utilizado")
    private Boolean utilizado = false;

    @Column(name = "fecha_utilizacion")
    private LocalDateTime fechaUtilizacion;

    /**
     * Marca el código como utilizado
     */
    public void marcarComoUtilizado() {
        this.utilizado = true;
        this.fechaUtilizacion = LocalDateTime.now();
    }

    /**
     * Verifica si el código está disponible
     */
    public boolean estaDisponible() {
        return !utilizado;
    }

    @PrePersist
    protected void onCreate() {
        if (fechaAutorizacion == null) {
            fechaAutorizacion = LocalDateTime.now();
        }
    }
}