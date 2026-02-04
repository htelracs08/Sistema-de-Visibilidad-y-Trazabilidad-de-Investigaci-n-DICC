package ec.epn.backend.domain;

public enum MotivoInactivoContrato {
    RENUNCIA("Renuncia del ayudante"),
    FIN_CONTRATO("Fin del período contractual"),
    DESPIDO("Despido por incumplimiento");

    private final String descripcion;

    MotivoInactivoContrato(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static MotivoInactivoContrato fromString(String motivo) {
        if (motivo == null) {
            throw new IllegalArgumentException("Motivo no puede ser null");
        }
        try {
            return MotivoInactivoContrato.valueOf(motivo.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Motivo inválido: " + motivo);
        }
    }
}