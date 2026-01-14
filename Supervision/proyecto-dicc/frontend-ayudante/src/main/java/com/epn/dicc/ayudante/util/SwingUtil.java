package com.epn.dicc.ayudante.util;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Utilidades para componentes Swing
 */
public class SwingUtil {
    
    /**
     * Centra una ventana en la pantalla
     */
    public static void centrarVentana(Window ventana) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - ventana.getWidth()) / 2;
        int y = (screenSize.height - ventana.getHeight()) / 2;
        ventana.setLocation(x, y);
    }
    
    /**
     * Muestra un mensaje de información
     */
    public static void mostrarInfo(Component parent, String mensaje) {
        JOptionPane.showMessageDialog(parent, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Muestra un mensaje de error
     */
    public static void mostrarError(Component parent, String mensaje) {
        JOptionPane.showMessageDialog(parent, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Muestra un mensaje de advertencia
     */
    public static void mostrarAdvertencia(Component parent, String mensaje) {
        JOptionPane.showMessageDialog(parent, mensaje, "Advertencia", JOptionPane.WARNING_MESSAGE);
    }
    
    /**
     * Muestra un diálogo de confirmación
     */
    public static boolean confirmar(Component parent, String mensaje) {
        int result = JOptionPane.showConfirmDialog(
                parent, 
                mensaje, 
                "Confirmación", 
                JOptionPane.YES_NO_OPTION
        );
        return result == JOptionPane.YES_OPTION;
    }
    
    /**
     * Muestra un diálogo de entrada de texto
     */
    public static String pedirTexto(Component parent, String mensaje) {
        return JOptionPane.showInputDialog(parent, mensaje);
    }
    
    /**
     * Limpia una tabla
     */
    public static void limpiarTabla(JTable tabla) {
        DefaultTableModel model = (DefaultTableModel) tabla.getModel();
        model.setRowCount(0);
    }
    
    /**
     * Crea un panel con borde y título
     */
    public static JPanel crearPanelConBorde(String titulo) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(titulo));
        return panel;
    }
    
    /**
     * Ejecuta una tarea en segundo plano con loading
     */
    public static void ejecutarConLoading(Component parent, String mensaje, Runnable tarea) {
        JDialog loadingDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), "Procesando...", true);
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(new JLabel(mensaje), BorderLayout.CENTER);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        panel.add(progressBar, BorderLayout.SOUTH);
        loadingDialog.add(panel);
        loadingDialog.pack();
        loadingDialog.setLocationRelativeTo(parent);
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                tarea.run();
                return null;
            }
            
            @Override
            protected void done() {
                loadingDialog.dispose();
            }
        };
        
        worker.execute();
        loadingDialog.setVisible(true);
    }
    
    /**
     * Deshabilita un componente y todos sus hijos
     */
    public static void deshabilitarComponentes(Container container, boolean deshabilitar) {
        for (Component comp : container.getComponents()) {
            comp.setEnabled(!deshabilitar);
            if (comp instanceof Container) {
                deshabilitarComponentes((Container) comp, deshabilitar);
            }
        }
    }
    
    /**
     * Aplica un Look and Feel moderno
     */
    public static void aplicarLookAndFeel() {
        try {
            // Usar FlatLaf (requiere dependencia com.formdev:flatlaf)
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
        } catch (Exception e) {
            // Si falla, usar el L&F del sistema
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}