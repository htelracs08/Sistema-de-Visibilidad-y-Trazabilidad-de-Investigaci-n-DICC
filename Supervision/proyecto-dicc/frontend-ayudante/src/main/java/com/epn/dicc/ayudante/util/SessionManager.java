package com.epn.dicc.ayudante.util;

import com.epn.dicc.ayudante.api.ApiClient;
import com.epn.dicc.ayudante.model.TokenResponse;
import com.epn.dicc.ayudante.model.UsuarioResponse;

/**
 * Gestor de sesión del usuario
 */
public class SessionManager {
    
    private static SessionManager instance;
    
    private TokenResponse token;
    private UsuarioResponse usuario;
    private ApiClient apiClient;
    
    private SessionManager() {
        this.apiClient = new ApiClient();
    }
    
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Inicia sesión con el token
     */
    public void login(TokenResponse token) {
        this.token = token;
        this.apiClient.setToken(token.getToken());
    }
    
    /**
     * Establece los datos del usuario
     */
    public void setUsuario(UsuarioResponse usuario) {
        this.usuario = usuario;
    }
    
    /**
     * Cierra la sesión
     */
    public void logout() {
        this.token = null;
        this.usuario = null;
        this.apiClient.setToken(null);
    }
    
    /**
     * Verifica si hay sesión activa
     */
    public boolean isLoggedIn() {
        return token != null && token.getToken() != null;
    }
    
    /**
     * Obtiene el token actual
     */
    public TokenResponse getToken() {
        return token;
    }
    
    /**
     * Obtiene el usuario actual
     */
    public UsuarioResponse getUsuario() {
        return usuario;
    }
    
    /**
     * Obtiene el cliente API
     */
    public ApiClient getApiClient() {
        return apiClient;
    }
    
    /**
     * Obtiene el ID del usuario
     */
    public Long getUserId() {
        return usuario != null ? usuario.getId() : null;
    }
    
    /**
     * Obtiene el nombre completo del usuario
     */
    public String getNombreCompleto() {
        return usuario != null ? usuario.getNombreCompleto() : "";
    }
}