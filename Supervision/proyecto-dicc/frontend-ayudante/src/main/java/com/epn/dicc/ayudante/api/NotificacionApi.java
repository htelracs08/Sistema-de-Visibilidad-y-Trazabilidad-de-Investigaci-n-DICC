package com.epn.dicc.ayudante.api;

import com.epn.dicc.ayudante.model.NotificacionResponse;

import java.io.IOException;
import java.util.List;

/**
 * API para gestión de notificaciones
 */
public class NotificacionApi {
    
    private final ApiClient apiClient;
    
    public NotificacionApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }
    
    /**
     * Listar notificaciones del usuario
     */
    public List<NotificacionResponse> listarPorUsuario(Long usuarioId) throws IOException {
        String endpoint = "/notificaciones/usuario/" + usuarioId;
        ApiClient.ApiResponse<List> response = apiClient.get(endpoint, List.class);
        
        if (response.isSuccess()) {
            return (List<NotificacionResponse>) response.getData();
        }
        
        throw new ApiClient.ApiException(response.getMessage());
    }
    
    /**
     * Listar notificaciones no leídas
     */
    public List<NotificacionResponse> listarNoLeidas(Long usuarioId) throws IOException {
        String endpoint = "/notificaciones/no-leidas/" + usuarioId;
        ApiClient.ApiResponse<List> response = apiClient.get(endpoint, List.class);
        
        if (response.isSuccess()) {
            return (List<NotificacionResponse>) response.getData();
        }
        
        throw new ApiClient.ApiException(response.getMessage());
    }
    
    /**
     * Contar notificaciones no leídas
     */
    public Integer contarNoLeidas(Long usuarioId) throws IOException {
        String endpoint = "/notificaciones/contar-no-leidas/" + usuarioId;
        ApiClient.ApiResponse<Integer> response = apiClient.get(endpoint, Integer.class);
        
        if (response.isSuccess()) {
            return response.getData();
        }
        
        return 0;
    }
    
    /**
     * Marcar notificación como leída
     */
    public void marcarComoLeida(Long notificacionId) throws IOException {
        String endpoint = "/notificaciones/" + notificacionId + "/marcar-leida";
        ApiClient.ApiResponse<Void> response = apiClient.post(endpoint, null, Void.class);
        
        if (!response.isSuccess()) {
            throw new ApiClient.ApiException(response.getMessage());
        }
    }
}