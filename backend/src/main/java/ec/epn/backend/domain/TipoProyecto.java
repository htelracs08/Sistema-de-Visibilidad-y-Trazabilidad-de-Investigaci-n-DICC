package ec.epn.backend.domain;

public enum TipoProyecto {
    INVESTIGACION("Proyecto de Investigación"),
    VINCULACION("Proyecto de Vinculación"),
    TRANSFERENCIA_TECNOLOGICA("Transferencia Tecnológica");

    private final String descripcion;

    TipoProyecto(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean requiereSubtipo() {
        return this == INVESTIGACION;
    }

    public static TipoProyecto fromString(String tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo de proyecto no puede ser null");
        }
        try {
            return TipoProyecto.valueOf(tipo.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de proyecto inválido: " + tipo);
        }
    }
}