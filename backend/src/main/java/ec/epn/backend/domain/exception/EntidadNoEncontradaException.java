package ec.epn.backend.domain.exception;

public class EntidadNoEncontradaException extends DominioException {
    
    private final String entidad;
    private final String id;

    public EntidadNoEncontradaException(String entidad, String id) {
        super(String.format("%s con id '%s' no encontrado", entidad, id));
        this.entidad = entidad;
        this.id = id;
    }

    public String getEntidad() {
        return entidad;
    }

    public String getId() {
        return id;
    }
}