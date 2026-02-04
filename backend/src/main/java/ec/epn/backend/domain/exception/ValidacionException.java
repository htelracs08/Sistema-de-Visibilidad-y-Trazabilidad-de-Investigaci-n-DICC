package ec.epn.backend.domain.exception;

import java.util.Map;
import java.util.HashMap;

public class ValidacionException extends DominioException {
    
    private final Map<String, String> errores;

    public ValidacionException(String mensaje) {
        super(mensaje);
        this.errores = new HashMap<>();
    }

    public ValidacionException(String mensaje, Map<String, String> errores) {
        super(mensaje);
        this.errores = errores;
    }

    public Map<String, String> getErrores() {
        return errores;
    }

    public void agregarError(String campo, String error) {
        errores.put(campo, error);
    }
}