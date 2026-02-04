package ec.epn.backend.domain.exception;

public class OperacionNoPermitidaException extends DominioException {
    
    public OperacionNoPermitidaException(String mensaje) {
        super(mensaje);
    }
}