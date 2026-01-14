package com.epn.dicc.ayudante.api;

import com.epn.dicc.ayudante.model.ContratoResponse;
import com.epn.dicc.ayudante.model.SolicitudIngresoRequest;

import java.io.IOException;
import java.util.List;

/**
 * API para gestión de contratos
 */
public class ContratoApi {
    
    private final ApiClient apiClient;
    
    public ContratoApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }
    
    /**
     * Solicitar ingreso a un proyecto
     */
    public ContratoResponse solicitarIngreso(Long ayudanteId, SolicitudIngresoRequest request) 
            throws IOException {
        String endpoint = "/contratos/solicitar?ayudanteId=" + ayudanteId;
        ApiClient.ApiResponse<ContratoResponse> response = apiClient.post(
                endpoint, 
                request, 
                ContratoResponse.class
        );
        
        if (response.isSuccess()) {
            return response.getData();
        }
        
        throw new ApiClient.ApiException(response.getMessage());
    }
    
    /**
     * Listar contratos de un ayudante
     */
    public List<ContratoResponse> listarPorAyudante(Long ayudanteId) throws IOException {
        String endpoint = "/contratos/ayudante/" + ayudanteId;
        
        // Usar TypeToken para List
        java.lang.reflect.Type listType = com.google.gson.reflect.TypeToken
                .getParameterized(List.class, ContratoResponse.class).getType();
        
        ApiClient.ApiResponse response = apiClient.get(endpoint, List.class);
        
        if (response.isSuccess()) {
            // Necesitarás parsear manualmente o ajustar el método get
            return (List<ContratoResponse>) response.getData();
        }
        
        throw new ApiClient.ApiException(response.getMessage());
    }
    
    /**
     * Obtener contrato por ID
     */
    public ContratoResponse obtenerPorId(Long contratoId) throws IOException {
        String endpoint = "/contratos/" + contratoId;
        ApiClient.ApiResponse<ContratoResponse> response = apiClient.get(
                endpoint, 
                ContratoResponse.class
        );
        
        if (response.isSuccess()) {
            return response.getData();
        }
        
        throw new ApiClient.ApiException(response.getMessage());
    }
    
    /**
     * Registrar renuncia
     */
    public void registrarRenuncia(Long contratoId, java.time.LocalDate fecha, String motivo) 
            throws IOException {
        String endpoint = "/contratos/" + contratoId + "/renuncia";
        
        // Crear request simple
        var request = new java.util.HashMap<String, Object>();
        request.put("fechaRenuncia", fecha);
        request.put("motivo", motivo);
        
        ApiClient.ApiResponse<Void> response = apiClient.post(endpoint, request, Void.class);
        
        if (!response.isSuccess()) {
            throw new ApiClient.ApiException(response.getMessage());
        }
    }
}
