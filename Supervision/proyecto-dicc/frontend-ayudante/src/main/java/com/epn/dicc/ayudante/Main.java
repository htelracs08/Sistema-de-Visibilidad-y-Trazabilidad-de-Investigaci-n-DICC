package com.epn.dicc.ayudante;

import com.epn.dicc.ayudante.util.SwingUtil;
import com.epn.dicc.ayudante.view.LoginFrame;

import javax.swing.*;

/**
 * Clase principal de la aplicación Ayudante
 */
public class Main {
    
    public static void main(String[] args) {
        // Mostrar mensaje en consola
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║                                                          ║");
        System.out.println("║     Sistema de Gestión de Proyectos - AYUDANTES         ║");
        System.out.println("║                      DICC - EPN                          ║");
        System.out.println("║                                                          ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Iniciando aplicación...");
        System.out.println("Backend: http://localhost:8080");
        System.out.println();
        
        // Configurar Look and Feel
        SwingUtilities.invokeLater(() -> {
            try {
                // Aplicar tema moderno
                SwingUtil.aplicarLookAndFeel();
                
                // Crear y mostrar ventana de login
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
                
                System.out.println("✓ Aplicación iniciada correctamente");
                
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                        "Error al iniciar la aplicación: " + e.getMessage(),
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}