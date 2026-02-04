package ec.epn.backend.config;

import ec.epn.backend.domain.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DominioException.class)
    public ResponseEntity<Map<String, Object>> handleDominioException(DominioException ex) {
        log.warn("Excepción de dominio: {}", ex.getMessage());
        return ResponseEntity
            .badRequest()
            .body(Map.of("ok", false, "msg", ex.getMessage()));
    }

    @ExceptionHandler(EntidadNoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> handleEntidadNoEncontrada(
        EntidadNoEncontradaException ex
    ) {
        log.warn("Entidad no encontrada: {} - {}", ex.getEntidad(), ex.getId());
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(Map.of(
                "ok", false,
                "msg", ex.getMessage(),
                "entidad", ex.getEntidad(),
                "id", ex.getId()
            ));
    }

    @ExceptionHandler(OperacionNoPermitidaException.class)
    public ResponseEntity<Map<String, Object>> handleOperacionNoPermitida(
        OperacionNoPermitidaException ex
    ) {
        log.warn("Operación no permitida: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(Map.of("ok", false, "msg", ex.getMessage()));
    }

    @ExceptionHandler(CupoAgotadoException.class)
    public ResponseEntity<Map<String, Object>> handleCupoAgotado(CupoAgotadoException ex) {
        log.warn("Cupo agotado: {}/{}", ex.getActivos(), ex.getMaximo());
        return ResponseEntity
            .badRequest()
            .body(Map.of(
                "ok", false,
                "msg", ex.getMessage(),
                "activos", ex.getActivos(),
                "maximo", ex.getMaximo()
            ));
    }

    @ExceptionHandler(ValidacionException.class)
    public ResponseEntity<Map<String, Object>> handleValidacionException(
        ValidacionException ex
    ) {
        log.warn("Errores de validación: {}", ex.getErrores());
        return ResponseEntity
            .badRequest()
            .body(Map.of(
                "ok", false,
                "msg", ex.getMessage(),
                "errores", ex.getErrores()
            ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
        MethodArgumentNotValidException ex
    ) {
        var errores = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                fieldError -> fieldError.getDefaultMessage() != null 
                    ? fieldError.getDefaultMessage() 
                    : "Error de validación",
                (a, b) -> a + "; " + b
            ));

        log.warn("Errores de validación: {}", errores);
        return ResponseEntity
            .badRequest()
            .body(Map.of(
                "ok", false,
                "msg", "Errores de validación",
                "errores", errores
            ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
        IllegalArgumentException ex
    ) {
        log.warn("Argumento ilegal: {}", ex.getMessage());
        return ResponseEntity
            .badRequest()
            .body(Map.of("ok", false, "msg", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(
        IllegalStateException ex
    ) {
        log.warn("Estado ilegal: {}", ex.getMessage());
        return ResponseEntity
            .badRequest()
            .body(Map.of("ok", false, "msg", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        log.error("Error inesperado", ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of(
                "ok", false,
                "msg", "Error interno del servidor"
            ));
    }
}