package com.epn.dicc.model.base;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Clase base para todas las entidades del sistema
 * Proporciona campos comunes de auditoría
 */
@Data
@MappedSuperclass
public abstract class EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @Column(name = "creado_por", length = 100)
    private String creadoPor;

    @Column(name = "modificado_por", length = 100)
    private String modificadoPor;

    @Column(name = "activo")
    private Boolean activo = true;

    /**
     * Marca la entidad como eliminada (soft delete)
     */
    public void marcarComoEliminado() {
        this.activo = false;
    }

    /**
     * Verifica si la entidad está activa
     */
    public boolean estaActivo() {
        return this.activo != null && this.activo;
    }
}