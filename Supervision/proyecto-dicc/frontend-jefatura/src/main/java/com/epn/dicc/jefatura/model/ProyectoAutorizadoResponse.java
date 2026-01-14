package com.epn.dicc.jefatura.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProyectoAutorizadoResponse {
    private Long id;
    private String codigoProyecto;
    private LocalDateTime fechaAutorizacion;
    private String autorizadoPor;
    private Boolean utilizado;
    private LocalDateTime fechaUtilizacion;
}