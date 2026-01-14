package com.epn.dicc.ayudante.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Response de token JWT
 */
@Data
public class TokenResponse {
    private String token;
    private String tipoToken;
    private LocalDateTime fechaExpiracion;
    private Long usuarioId;
    private String nombreCompleto;
    private String rol;
}