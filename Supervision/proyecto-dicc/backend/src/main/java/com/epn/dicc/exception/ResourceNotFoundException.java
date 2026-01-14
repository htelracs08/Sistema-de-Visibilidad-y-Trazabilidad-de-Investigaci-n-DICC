package com.epn.dicc.exception;

/**
 * Excepción de recurso no encontrado
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}