package com.epn.dicc.ayudante.api;

import com.epn.dicc.ayudante.model.LoginRequest;
import com.epn.dicc.ayudante.model.RegistroRequest;
import com.epn.dicc.ayudante.model.TokenResponse;
import com.epn.dicc.ayudante.model.UsuarioResponse;

import java.io.IOException;

/**
 * API específica para autenticación
 */
public class AuthApi {
    
    private final ApiClient apiClient;
    
    public AuthApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }
    
    /**
     * Login de usuario
     */
    public TokenResponse login(String correo, String password) throws IOException {
        LoginRequest request = new LoginRequest(correo, password);
        ApiClient.ApiResponse<TokenResponse> response = apiClient.post(
                "/auth/login", 
                request, 
                TokenResponse.class
        );
        
        if (response.isSuccess() && response.getData() != null) {
            // Guardar token en el cliente
            apiClient.setToken(response.getData().getToken());
            return response.getData();
        }
        
        throw new ApiClient.ApiException(response.getMessage());
    }
    
    /**
     * Registro de usuario
     */
    public UsuarioResponse registrar(RegistroRequest request) throws IOException {
        ApiClient.ApiResponse<UsuarioResponse> response = apiClient.post(
                "/auth/register", 
                request, 
                UsuarioResponse.class
        );
        
        if (response.isSuccess()) {
            return response.getData();
        }
        
        throw new ApiClient.ApiException(response.getMessage());
    }
    
    /**
     * Verificar si la API está funcionando
     */
    public boolean verificarConexion() {
        try {
            ApiClient.ApiResponse<String> response = apiClient.get("/auth/health", String.class);
            return response.isSuccess();
        } catch (IOException e) {
            return false;
        }
    }
}