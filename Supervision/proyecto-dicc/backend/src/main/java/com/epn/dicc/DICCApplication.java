package com.epn.dicc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Clase principal de la aplicación Spring Boot
 * Sistema de Gestión de Proyectos y Ayudantes - DICC
 * 
 * @author Proyecto DICC
 * @version 1.0.0
 */
@SpringBootApplication
public class DICCApplication {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║                                                          ║");
        System.out.println("║     Sistema de Gestión de Proyectos y Ayudantes         ║");
        System.out.println("║                      DICC - EPN                          ║");
        System.out.println("║                                                          ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Iniciando servidor backend...");
        System.out.println();

        SpringApplication.run(DICCApplication.class, args);

        System.out.println();
        System.out.println("✓ Servidor iniciado correctamente");
        System.out.println("✓ API REST disponible en: http://localhost:8080");
        System.out.println("✓ Base de datos SQLite: database.db");
        System.out.println();
        System.out.println("Endpoints principales:");
        System.out.println("  - POST   /api/auth/login");
        System.out.println("  - POST   /api/auth/register");
        System.out.println("  - GET    /api/proyectos");
        System.out.println("  - GET    /api/estadisticas/kpi");
        System.out.println();
        System.out.println("Presiona Ctrl+C para detener el servidor");
    }

    /**
     * Bean para encriptar contraseñas con BCrypt
     */
    /* 
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    */
}