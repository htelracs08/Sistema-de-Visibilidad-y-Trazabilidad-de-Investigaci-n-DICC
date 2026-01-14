package com.epn.dicc.jefatura.api;

import com.epn.dicc.jefatura.model.ProyectoAutorizadoResponse;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * API para autorización de proyectos
 */
public class AutorizacionProyectoApi {
    
    private final ApiClient apiClient;
    
    public AutorizacionProyectoApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }
    
    /**
     * Autorizar un proyecto manualmente
     */
    public ProyectoAutorizadoResponse autorizarProyecto(String codigoProyecto, Long jefaturaId) 
            throws IOException {
        String endpoint = "/autorizacion-proyectos/autorizar?codigoProyecto=" + 
                         codigoProyecto + "&jefaturaId=" + jefaturaId;
        
        ApiClient.ApiResponse<ProyectoAutorizadoResponse> response = apiClient.post(
                endpoint, 
                null, 
                ProyectoAutorizadoResponse.class
        );
        
        if (response.isSuccess()) {
            return response.getData();
        }
        
        throw new ApiClient.ApiException(response.getMessage());
    }
    
    /**
     * Listar proyectos autorizados disponibles
     */
    public List<ProyectoAutorizadoResponse> listarDisponibles() throws IOException {
        String endpoint = "/autorizacion-proyectos/disponibles";
        ApiClient.ApiResponse<List> response = apiClient.get(endpoint, List.class);
        
        if (response.isSuccess()) {
            return (List<ProyectoAutorizadoResponse>) response.getData();
        }
        
        throw new ApiClient.ApiException(response.getMessage());
    }
    
    /**
     * Eliminar una autorización
     */
    public void eliminarAutorizacion(String codigoProyecto) throws IOException {
        String endpoint = "/autorizacion-proyectos/" + codigoProyecto;
        ApiClient.ApiResponse<Void> response = apiClient.delete(endpoint, Void.class);
        
        if (!response.isSuccess()) {
            throw new ApiClient.ApiException(response.getMessage());
        }
    }
}