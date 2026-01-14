package com.epn.dicc.ayudante.api;

import com.epn.dicc.ayudante.model.ProyectoResponse;

import java.io.IOException;
import java.util.List;

/**
 * API para consultar proyectos
 */
public class ProyectoApi {
    
    private final ApiClient apiClient;
    
    public ProyectoApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }
    
    /**
     * Listar proyectos activos
     */
    public List<ProyectoResponse> listarActivos() throws IOException {
        String endpoint = "/proyectos/activos";
        ApiClient.ApiResponse<List> response = apiClient.get(endpoint, List.class);
        
        if (response.isSuccess()) {
            return (List<ProyectoResponse>) response.getData();
        }
        
        throw new ApiClient.ApiException(response.getMessage());
    }
    
    /**
     * Buscar proyecto por código
     */
    public ProyectoResponse buscarPorCodigo(String codigo) throws IOException {
        String endpoint = "/proyectos/codigo/" + codigo;
        ApiClient.ApiResponse<ProyectoResponse> response = apiClient.get(
                endpoint, 
                ProyectoResponse.class
        );
        
        if (response.isSuccess()) {
            return response.getData();
        }
        
        throw new ApiClient.ApiException(response.getMessage());
    }
    
    /**
     * Obtener proyecto por ID
     */
    public ProyectoResponse obtenerPorId(Long proyectoId) throws IOException {
        String endpoint = "/proyectos/" + proyectoId;
        ApiClient.ApiResponse<ProyectoResponse> response = apiClient.get(
                endpoint, 
                ProyectoResponse.class
        );
        
        if (response.isSuccess()) {
            return response.getData();
        }
        
        throw new ApiClient.ApiException(response.getMessage());
    }
}
