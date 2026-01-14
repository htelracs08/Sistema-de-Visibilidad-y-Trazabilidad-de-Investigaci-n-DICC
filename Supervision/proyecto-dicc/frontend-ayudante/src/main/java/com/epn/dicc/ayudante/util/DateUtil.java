package com.epn.dicc.ayudante.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilidades para fechas
 */
public class DateUtil {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    /**
     * Formatea una fecha
     */
    public static String formatearFecha(LocalDate fecha) {
        if (fecha == null) return "";
        return fecha.format(DATE_FORMATTER);
    }
    
    /**
     * Formatea una fecha y hora
     */
    public static String formatearFechaHora(LocalDateTime fechaHora) {
        if (fechaHora == null) return "";
        return fechaHora.format(DATETIME_FORMATTER);
    }
    
    /**
     * Parsea una fecha
     */
    public static LocalDate parsearFecha(String fecha) {
        if (fecha == null || fecha.isEmpty()) return null;
        return LocalDate.parse(fecha, DATE_FORMATTER);
    }
    
    /**
     * Obtiene el nombre del mes
     */
    public static String getNombreMes(int mes) {
        String[] meses = {
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        };
        return mes >= 1 && mes <= 12 ? meses[mes - 1] : "";
    }
}