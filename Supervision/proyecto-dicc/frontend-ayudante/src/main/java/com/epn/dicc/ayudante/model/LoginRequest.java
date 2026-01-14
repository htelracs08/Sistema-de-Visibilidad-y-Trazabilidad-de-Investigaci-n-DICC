package com.epn.dicc.ayudante.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request para login
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    private String correoInstitucional;
    private String password;
}
