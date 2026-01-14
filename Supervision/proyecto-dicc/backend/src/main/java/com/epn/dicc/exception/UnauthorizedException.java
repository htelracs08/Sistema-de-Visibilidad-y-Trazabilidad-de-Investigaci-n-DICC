package com.epn.dicc.exception;

/**
 * Excepción de no autorizado
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}