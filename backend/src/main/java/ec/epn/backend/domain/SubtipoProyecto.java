package ec.epn.backend.domain;

public enum SubtipoProyecto {
    INTERNO("Proyecto Interno"),
    EXTERNO("Proyecto Externo"),
    SEMILLA("Proyecto Semilla"),
    GRUPAL("Proyecto Grupal"),
    MULTIDISCIPLINARIO("Proyecto Multidisciplinario");

    private final String descripcion;

    SubtipoProyecto(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static SubtipoProyecto fromString(String subtipo) {
        if (subtipo == null || subtipo.isBlank()) {
            return null;
        }
        try {
            return SubtipoProyecto.valueOf(subtipo.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Subtipo de proyecto inválido: " + subtipo);
        }
    }
}