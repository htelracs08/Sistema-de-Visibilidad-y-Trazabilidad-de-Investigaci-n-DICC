
package com.epn.dicc.ayudante.view;

import com.epn.dicc.ayudante.util.SessionManager;
import com.epn.dicc.ayudante.util.SwingUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana principal de la aplicación Ayudante
 */
public class MainFrame extends JFrame {
    
    private JTabbedPane tabbedPane;
    private MisContratosPanel contratosPanel;
    private BitacorasPanel bitacorasPanel;
    private NotificacionesPanel notificacionesPanel;
    
    public MainFrame() {
        initComponents();
        cargarDatos();
    }
    
    private void initComponents() {
        setTitle("DICC - Sistema de Ayudantes");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        
        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Panel superior con información del usuario
        JPanel topPanel = createTopPanel();
        
        // Panel con tabs
        tabbedPane = new JTabbedPane();
        
        // Tab 1: Mis Contratos
        contratosPanel = new MisContratosPanel();
        tabbedPane.addTab("Mis Contratos", contratosPanel);
        
        // Tab 2: Bitácoras
        bitacorasPanel = new BitacorasPanel();
        tabbedPane.addTab("Bitácoras", bitacorasPanel);
        
        // Tab 3: Notificaciones
        notificacionesPanel = new NotificacionesPanel();
        tabbedPane.addTab("Notificaciones", notificacionesPanel);
        
        // Agregar componentes
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        add(mainPanel);
        
        SwingUtil.centrarVentana(this);
        
        // Agregar WindowListener para confirmar cierre
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cerrarSesion();
            }
        });
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(240, 240, 240));
        
        // Información del usuario
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setOpaque(false);
        
        String nombreUsuario = SessionManager.getInstance().getNombreCompleto();
        JLabel lblBienvenida = new JLabel("Bienvenido, " + nombreUsuario);
        lblBienvenida.setFont(lblBienvenida.getFont().deriveFont(Font.BOLD, 14f));
        infoPanel.add(lblBienvenida);
        
        // Botones de acción
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        
        JButton btnSolicitarIngreso = new JButton("Solicitar Ingreso a Proyecto");
        btnSolicitarIngreso.addActionListener(e -> solicitarIngreso());
        buttonPanel.add(btnSolicitarIngreso);
        
        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> actualizarDatos());
        buttonPanel.add(btnActualizar);
        
        JButton btnCerrarSesion = new JButton("Cerrar Sesión");
        btnCerrarSesion.addActionListener(e -> cerrarSesion());
        buttonPanel.add(btnCerrarSesion);
        
        panel.add(infoPanel, BorderLayout.WEST);
        panel.add(buttonPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private void cargarDatos() {
        // Cargar datos iniciales
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                contratosPanel.cargarContratos();
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception e) {
                    SwingUtil.mostrarError(MainFrame.this, 
                            "Error al cargar datos: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private void solicitarIngreso() {
        SolicitarIngresoDialog dialog = new SolicitarIngresoDialog(this);
        dialog.setVisible(true);
        
        // Si se envió la solicitud, actualizar
        if (dialog.isEnviado()) {
            actualizarDatos();
        }
    }
    
    private void actualizarDatos() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                contratosPanel.cargarContratos();
                bitacorasPanel.actualizarDatos();
                notificacionesPanel.cargarNotificaciones();
                return null;
            }
            
            @Override
            protected void done() {
                SwingUtil.mostrarInfo(MainFrame.this, "Datos actualizados");
            }
        };
        
        worker.execute();
    }
    
    private void cerrarSesion() {
        if (SwingUtil.confirmar(this, "¿Está seguro que desea cerrar sesión?")) {
            SessionManager.getInstance().logout();
            
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            this.dispose();
        }
    }
}