package ec.epn.backend.domain;

public enum EstadoContrato {
    ACTIVO("Contrato activo"),
    INACTIVO("Contrato inactivo");

    private final String descripcion;

    EstadoContrato(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean puedeFinalizarse() {
        return this == ACTIVO;
    }

    public static EstadoContrato fromString(String estado) {
        if (estado == null) {
            throw new IllegalArgumentException("Estado no puede ser null");
        }
        try {
            return EstadoContrato.valueOf(estado.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado de contrato inválido: " + estado);
        }
    }
}