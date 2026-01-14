package com.epn.dicc.jefatura.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Cliente base para consumir la API REST
 */
public class ApiClient {
    
    private static final String BASE_URL = "http://localhost:8080/api";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final OkHttpClient client;
    private final Gson gson;
    private String token;
    
    public ApiClient() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }
    
    /**
     * Establece el token de autenticación
     */
    public void setToken(String token) {
        this.token = token;
    }
    
    /**
     * Obtiene el token actual
     */
    public String getToken() {
        return token;
    }
    
    /**
     * Realiza una petición POST
     */
    public <T> ApiResponse<T> post(String endpoint, Object requestBody, Class<T> responseClass) throws IOException {
        String url = BASE_URL + endpoint;
        String jsonBody = gson.toJson(requestBody);
        
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(body);
        
        if (token != null && !token.isEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        }
        
        Request request = requestBuilder.build();
        
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            
            if (!response.isSuccessful()) {
                // Intentar parsear error
                ApiResponse<T> errorResponse = gson.fromJson(responseBody, ApiResponse.class);
                if (errorResponse != null && errorResponse.getMessage() != null) {
                    throw new ApiException(errorResponse.getMessage());
                }
                throw new ApiException("Error en la petición: " + response.code());
            }
            
            // Parsear respuesta exitosa
            java.lang.reflect.Type type = com.google.gson.reflect.TypeToken
                    .getParameterized(ApiResponse.class, responseClass).getType();
            return gson.fromJson(responseBody, type);
        }
    }
    
    /**
     * Realiza una petición GET
     */
    public <T> ApiResponse<T> get(String endpoint, Class<T> responseClass) throws IOException {
        String url = BASE_URL + endpoint;
        
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .get();
        
        if (token != null && !token.isEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        }
        
        Request request = requestBuilder.build();
        
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            
            if (!response.isSuccessful()) {
                ApiResponse<T> errorResponse = gson.fromJson(responseBody, ApiResponse.class);
                if (errorResponse != null && errorResponse.getMessage() != null) {
                    throw new ApiException(errorResponse.getMessage());
                }
                throw new ApiException("Error en la petición: " + response.code());
            }
            
            java.lang.reflect.Type type = com.google.gson.reflect.TypeToken
                    .getParameterized(ApiResponse.class, responseClass).getType();
            return gson.fromJson(responseBody, type);
        }
    }
    
    /**
     * Realiza una petición PUT
     */
    public <T> ApiResponse<T> put(String endpoint, Object requestBody, Class<T> responseClass) throws IOException {
        String url = BASE_URL + endpoint;
        String jsonBody = gson.toJson(requestBody);
        
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .put(body);
        
        if (token != null && !token.isEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        }
        
        Request request = requestBuilder.build();
        
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            
            if (!response.isSuccessful()) {
                ApiResponse<T> errorResponse = gson.fromJson(responseBody, ApiResponse.class);
                if (errorResponse != null && errorResponse.getMessage() != null) {
                    throw new ApiException(errorResponse.getMessage());
                }
                throw new ApiException("Error en la petición: " + response.code());
            }
            
            java.lang.reflect.Type type = com.google.gson.reflect.TypeToken
                    .getParameterized(ApiResponse.class, responseClass).getType();
            return gson.fromJson(responseBody, type);
        }
    }
    
    /**
     * Realiza una petición DELETE
     */
    public <T> ApiResponse<T> delete(String endpoint, Class<T> responseClass) throws IOException {
        String url = BASE_URL + endpoint;
        
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .delete();
        
        if (token != null && !token.isEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        }
        
        Request request = requestBuilder.build();
        
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            
            if (!response.isSuccessful()) {
                ApiResponse<T> errorResponse = gson.fromJson(responseBody, ApiResponse.class);
                if (errorResponse != null && errorResponse.getMessage() != null) {
                    throw new ApiException(errorResponse.getMessage());
                }
                throw new ApiException("Error en la petición: " + response.code());
            }
            
            java.lang.reflect.Type type = com.google.gson.reflect.TypeToken
                    .getParameterized(ApiResponse.class, responseClass).getType();
            return gson.fromJson(responseBody, type);
        }
    }
    
    /**
     * Clase para respuestas de la API
     */
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public T getData() { return data; }
    }
    
    /**
     * Excepción personalizada para errores de API
     */
    public static class ApiException extends IOException {
        public ApiException(String message) {
            super(message);
        }
    }
    
    // Adaptadores para LocalDate y LocalDateTime
    private static class LocalDateAdapter extends com.google.gson.TypeAdapter<LocalDate> {
        @Override
        public void write(com.google.gson.stream.JsonWriter out, LocalDate value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.toString());
            }
        }
        
        @Override
        public LocalDate read(com.google.gson.stream.JsonReader in) throws IOException {
            if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return LocalDate.parse(in.nextString());
        }
    }
    
    private static class LocalDateTimeAdapter extends com.google.gson.TypeAdapter<LocalDateTime> {
        @Override
        public void write(com.google.gson.stream.JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.toString());
            }
        }
        
        @Override
        public LocalDateTime read(com.google.gson.stream.JsonReader in) throws IOException {
            if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return LocalDateTime.parse(in.nextString());
        }
    }
}
