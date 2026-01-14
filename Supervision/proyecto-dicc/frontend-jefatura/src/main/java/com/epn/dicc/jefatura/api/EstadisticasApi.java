package com.epn.dicc.jefatura.api;

import com.epn.dicc.jefatura.model.KPIGlobalesResponse;

import java.io.IOException;

/**
 * API para estadísticas y KPIs
 */
public class EstadisticasApi {
    
    private final ApiClient apiClient;
    
    public EstadisticasApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }
    
    /**
     * Obtener KPIs globales
     */
    public KPIGlobalesResponse obtenerKPIGlobales() throws IOException {
        String endpoint = "/estadisticas/kpi";
        ApiClient.ApiResponse<KPIGlobalesResponse> response = apiClient.get(
                endpoint, 
                KPIGlobalesResponse.class
        );
        
        if (response.isSuccess()) {
            return response.getData();
        }
        
        throw new ApiClient.ApiException(response.getMessage());
    }
}