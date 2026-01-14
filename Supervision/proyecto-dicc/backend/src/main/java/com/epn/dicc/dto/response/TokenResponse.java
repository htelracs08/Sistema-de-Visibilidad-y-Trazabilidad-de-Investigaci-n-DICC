package com.epn.dicc.dto.response;

import com.epn.dicc.model.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para autenticación (Token JWT)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {
    private String token;
    private String tipoToken = "Bearer";
    private LocalDateTime fechaExpiracion;
    private Long usuarioId;
    private String nombreCompleto;
    private Rol rol;
}