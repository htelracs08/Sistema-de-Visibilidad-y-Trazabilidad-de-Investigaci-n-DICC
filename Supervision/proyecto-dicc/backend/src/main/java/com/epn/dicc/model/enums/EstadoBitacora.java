package com.epn.dicc.model.enums;

/**
 * Estados de una bitácora mensual
 */
public enum EstadoBitacora {
    BORRADOR("Borrador"),
    ENVIADA_REVISION("Enviada a Revisión"),
    APROBADA("Aprobada"),
    RECHAZADA("Rechazada"),
    REQUIERE_MODIFICACION("Requiere Modificación");

    private final String descripcion;

    EstadoBitacora(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean puedeEditar() {
        return this == BORRADOR || this == REQUIERE_MODIFICACION;
    }
}