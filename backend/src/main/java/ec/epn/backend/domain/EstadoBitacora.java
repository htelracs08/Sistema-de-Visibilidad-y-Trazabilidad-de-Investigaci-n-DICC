package ec.epn.backend.domain;

public enum EstadoBitacora {
    BORRADOR("Bitácora en edición", true),
    ENVIADA("Enviada al director", false),
    APROBADA("Aprobada por el director", false),
    RECHAZADA("Rechazada por el director", true);

    private final String descripcion;
    private final boolean editable;

    EstadoBitacora(String descripcion, boolean editable) {
        this.descripcion = descripcion;
        this.editable = editable;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean esEditable() {
        return editable;
    }

    public boolean puedeEnviarse() {
        return this == BORRADOR || this == RECHAZADA;
    }

    public boolean puedeRevisarse() {
        return this == ENVIADA;
    }

    public EstadoBitacora transicionarAEnviada() {
        if (!puedeEnviarse()) {
            throw new IllegalStateException(
                "No se puede enviar desde estado " + this
            );
        }
        return ENVIADA;
    }

    public EstadoBitacora transicionarAAprobar() {
        if (!puedeRevisarse()) {
            throw new IllegalStateException(
                "No se puede aprobar desde estado " + this
            );
        }
        return APROBADA;
    }

    public EstadoBitacora transicionarARechazar() {
        if (!puedeRevisarse()) {
            throw new IllegalStateException(
                "No se puede rechazar desde estado " + this
            );
        }
        return RECHAZADA;
    }

    public static EstadoBitacora fromString(String estado) {
        if (estado == null) {
            throw new IllegalArgumentException("Estado no puede ser null");
        }
        try {
            return EstadoBitacora.valueOf(estado.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado de bitácora inválido: " + estado);
        }
    }
}