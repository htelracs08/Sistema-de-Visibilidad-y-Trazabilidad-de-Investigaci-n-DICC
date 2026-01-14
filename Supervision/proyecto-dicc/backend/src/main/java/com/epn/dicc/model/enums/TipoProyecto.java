package com.epn.dicc.model.enums;

/**
 * Tipos de proyecto
 */
public enum TipoProyecto {
    INVESTIGACION("Investigación"),
    TRANSFERENCIA_TECNOLOGICA("Transferencia Tecnológica"),
    VINCULACION("Vinculación");

    private final String descripcion;

    TipoProyecto(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}