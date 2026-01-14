package com.epn.dicc.ayudante.api;

import com.epn.dicc.ayudante.model.AgregarActividadRequest;
import com.epn.dicc.ayudante.model.BitacoraResponse;
import com.epn.dicc.ayudante.model.CrearBitacoraRequest;

import java.io.IOException;
import java.util.List;

/**
 * API para gestión de bitácoras
 */
public class BitacoraApi {
    
    private final ApiClient apiClient;
    
    public BitacoraApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }
    
    /**
     * Crear nueva bitácora
     */
    public BitacoraResponse crear(CrearBitacoraRequest request) throws IOException {
        String endpoint = "/bitacoras";
        ApiClient.ApiResponse<BitacoraResponse> response = apiClient.post(
                endpoint, 
                request, 
                BitacoraResponse.class
        );
        
        if (response.isSuccess()) {
            return response.getData();
        }
        
        throw new ApiClient.ApiException(response.getMessage());
    }
    
    /**
     * Agregar actividad a bitácora
     */
    public void agregarActividad(AgregarActividadRequest request) throws IOException {
        String endpoint = "/bitacoras/actividades";
        ApiClient.ApiResponse<Void> response = apiClient.post(endpoint, request, Void.class);
        
        if (!response.isSuccess()) {
            throw new ApiClient.ApiException(response.getMessage());
        }
    }
    
    /**
     * Enviar bitácora a revisión
     */
    public void enviarRevision(Long bitacoraId) throws IOException {
        String endpoint = "/bitacoras/" + bitacoraId + "/enviar";
        ApiClient.ApiResponse<Void> response = apiClient.post(endpoint, null, Void.class);
        
        if (!response.isSuccess()) {
            throw new ApiClient.ApiException(response.getMessage());
        }
    }
    
    /**
     * Listar bitácoras de un contrato
     */
    public List<BitacoraResponse> listarPorContrato(Long contratoId) throws IOException {
        String endpoint = "/bitacoras/contrato/" + contratoId;
        ApiClient.ApiResponse<List> response = apiClient.get(endpoint, List.class);
        
        if (response.isSuccess()) {
            return (List<BitacoraResponse>) response.getData();
        }
        
        throw new ApiClient.ApiException(response.getMessage());
    }
    
    /**
     * Obtener bitácora por ID
     */
    public BitacoraResponse obtenerPorId(Long bitacoraId) throws IOException {
        String endpoint = "/bitacoras/" + bitacoraId;
        ApiClient.ApiResponse<BitacoraResponse> response = apiClient.get(
                endpoint, 
                BitacoraResponse.class
        );
        
        if (response.isSuccess()) {
            return response.getData();
        }
        
        throw new ApiClient.ApiException(response.getMessage());
    }
    
    /**
     * Eliminar actividad
     */
    public void eliminarActividad(Long actividadId) throws IOException {
        String endpoint = "/bitacoras/actividades/" + actividadId;
        ApiClient.ApiResponse<Void> response = apiClient.delete(endpoint, Void.class);
        
        if (!response.isSuccess()) {
            throw new ApiClient.ApiException(response.getMessage());
        }
    }
}