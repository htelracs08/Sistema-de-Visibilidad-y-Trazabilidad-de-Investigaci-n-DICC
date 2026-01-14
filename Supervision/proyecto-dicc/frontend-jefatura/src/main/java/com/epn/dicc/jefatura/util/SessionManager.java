package com.epn.dicc.jefatura.util;

import com.epn.dicc.jefatura.api.ApiClient;
import com.epn.dicc.jefatura.model.TokenResponse;
import com.epn.dicc.jefatura.model.UsuarioResponse;

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
    
    public void login(TokenResponse token) {
        this.token = token;
        this.apiClient.setToken(token.getToken());
    }
    
    public void setUsuario(UsuarioResponse usuario) {
        this.usuario = usuario;
    }
    
    public void logout() {
        this.token = null;
        this.usuario = null;
        this.apiClient.setToken(null);
    }
    
    public boolean isLoggedIn() {
        return token != null && token.getToken() != null;
    }
    
    public TokenResponse getToken() {
        return token;
    }
    
    public UsuarioResponse getUsuario() {
        return usuario;
    }
    
    public ApiClient getApiClient() {
        return apiClient;
    }
    
    public Long getUserId() {
        return usuario != null ? usuario.getId() : null;
    }
    
    public String getNombreCompleto() {
        return usuario != null ? usuario.getNombreCompleto() : "";
    }
}