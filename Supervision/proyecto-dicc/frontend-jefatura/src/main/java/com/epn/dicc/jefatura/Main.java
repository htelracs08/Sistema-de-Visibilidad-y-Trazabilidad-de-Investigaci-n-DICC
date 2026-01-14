package com.epn.dicc.jefatura;

import com.epn.dicc.jefatura.util.SwingUtil;
import com.epn.dicc.jefatura.view.LoginFrame;

import javax.swing.*;

/**
 * Clase principal de la aplicación Jefatura
 */
public class Main {
    
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║                                                          ║");
        System.out.println("║     Sistema de Gestión de Proyectos - JEFATURA          ║");
        System.out.println("║                      DICC - EPN                          ║");
        System.out.println("║                                                          ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Iniciando aplicación Jefatura...");
        System.out.println("Backend: http://localhost:8080");
        System.out.println();
        
        SwingUtilities.invokeLater(() -> {
            try {
                SwingUtil.aplicarLookAndFeel();
                
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
                
                System.out.println("✓ Aplicación Jefatura iniciada correctamente");
                
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