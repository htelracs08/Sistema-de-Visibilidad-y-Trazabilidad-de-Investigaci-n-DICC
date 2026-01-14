package com.epn.dicc.model.enums;

/**
 * Tipos de notificación
 */
public enum TipoNotificacion {
    PROYECTO_CREADO("Proyecto Creado"),
    SOLICITUD_INGRESO_AYUDANTE("Solicitud de Ingreso de Ayudante"),
    CONTRATO_APROBADO("Contrato Aprobado"),
    CONTRATO_RECHAZADO("Contrato Rechazado"),
    RENUNCIA_AYUDANTE("Renuncia de Ayudante"),
    FINALIZACION_PROYECTO("Finalización de Proyecto"),
    BITACORA_ENVIADA("Bitácora Enviada"),
    BITACORA_APROBADA("Bitácora Aprobada"),
    BITACORA_RECHAZADA("Bitácora Rechazada");

    private final String descripcion;

    TipoNotificacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}