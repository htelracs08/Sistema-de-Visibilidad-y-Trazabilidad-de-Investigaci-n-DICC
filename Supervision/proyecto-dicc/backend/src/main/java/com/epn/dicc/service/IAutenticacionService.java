package com.epn.dicc.service;

import com.epn.dicc.dto.request.LoginRequest;
import com.epn.dicc.dto.request.RegistroUsuarioRequest;
import com.epn.dicc.dto.response.TokenResponse;
import com.epn.dicc.dto.response.UsuarioResponse;
import com.epn.dicc.model.*;
import com.epn.dicc.model.enums.Rol;

/**
 * Interface del servicio de autenticación
 */
public interface IAutenticacionService {
    TokenResponse login(LoginRequest request);
    UsuarioResponse registrarUsuario(RegistroUsuarioRequest request);
    void validarCorreoInstitucional(String correo);
    boolean verificarCodigoJefatura(String codigo);
}