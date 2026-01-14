package com.epn.dicc.model.enums;

/**
 * Estados posibles de un proyecto
 */
public enum EstadoProyecto {
    AUTORIZADO_PENDIENTE("Autorizado - Pendiente de Inicio"),
    ACTIVO("Activo"),
    FINALIZADO("Finalizado"),
    SUSPENDIDO("Suspendido"),
    CANCELADO("Cancelado");

    private final String descripcion;

    EstadoProyecto(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
