package ec.epn.backend.domain;

public enum TipoAyudante {
    AYUDANTE_INVESTIGACION("Ayudante de Investigación"),
    AYUDANTE_DOCENCIA("Ayudante de Docencia");

    private final String descripcion;

    TipoAyudante(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static TipoAyudante fromString(String tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo de ayudante no puede ser null");
        }
        try {
            return TipoAyudante.valueOf(tipo.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de ayudante inválido: " + tipo);
        }
    }
}