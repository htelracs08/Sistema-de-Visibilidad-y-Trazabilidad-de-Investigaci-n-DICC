package com.epn.dicc.model.enums;

/**
 * Roles de usuario en el sistema
 */
public enum Rol {
    JEFATURA_DICC("Jefatura del DICC"),
    DIRECTOR_PROYECTO("Director de Proyecto"),
    AYUDANTE_PROYECTO("Ayudante de Proyecto");

    private final String descripcion;

    Rol(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}