package com.epn.dicc.model.enums;

/**
 * Estados de un contrato de ayudante
 */
public enum EstadoContrato {
    PENDIENTE_APROBACION_DIRECTOR("Pendiente de Aprobación del Director"),
    ACTIVO("Activo"),
    FINALIZADO_NORMAL("Finalizado Normalmente"),
    FINALIZADO_RENUNCIA("Finalizado por Renuncia"),
    RECHAZADO("Rechazado");

    private final String descripcion;

    EstadoContrato(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean esActivo() {
        return this == ACTIVO;
    }

    public boolean esFinalizado() {
        return this == FINALIZADO_NORMAL || this == FINALIZADO_RENUNCIA;
    }
}