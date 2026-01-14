package com.epn.dicc.exception;

/**
 * Excepción de negocio
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}