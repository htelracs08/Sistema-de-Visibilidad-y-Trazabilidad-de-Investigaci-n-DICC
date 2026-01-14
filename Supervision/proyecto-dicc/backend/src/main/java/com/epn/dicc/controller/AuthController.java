package com.epn.dicc.controller;

import com.epn.dicc.dto.request.LoginRequest;
import com.epn.dicc.dto.request.RegistroUsuarioRequest;
import com.epn.dicc.dto.response.ApiResponse;
import com.epn.dicc.dto.response.TokenResponse;
import com.epn.dicc.dto.response.UsuarioResponse;
import com.epn.dicc.service.IAutenticacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para autenticación
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // En producción, especifica los orígenes permitidos
public class AuthController {

    private final IAutenticacionService autenticacionService;

    /**
     * Endpoint de login
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse token = autenticacionService.login(request);
        return ResponseEntity.ok(
                ApiResponse.success("Login exitoso", token)
        );
    }

    /**
     * Endpoint de registro
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UsuarioResponse>> register(@Valid @RequestBody RegistroUsuarioRequest request) {
        UsuarioResponse usuario = autenticacionService.registrarUsuario(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Usuario registrado exitosamente", usuario)
        );
    }

    /**
     * Endpoint para verificar código de jefatura
     * POST /api/auth/verificar-codigo-jefatura
     */
    @PostMapping("/verificar-codigo-jefatura")
    public ResponseEntity<ApiResponse<Boolean>> verificarCodigoJefatura(@RequestBody String codigo) {
        boolean esValido = autenticacionService.verificarCodigoJefatura(codigo);
        return ResponseEntity.ok(
                ApiResponse.success("Código verificado", esValido)
        );
    }

    /**
     * Endpoint de health check
     * GET /api/auth/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success("API funcionando correctamente", "OK")
        );
    }
}