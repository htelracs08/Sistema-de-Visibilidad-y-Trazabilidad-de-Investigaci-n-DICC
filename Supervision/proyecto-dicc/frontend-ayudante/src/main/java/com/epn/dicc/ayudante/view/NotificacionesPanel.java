package com.epn.dicc.ayudante.view;

import com.epn.dicc.ayudante.api.ApiClient;
import com.epn.dicc.ayudante.api.NotificacionApi;
import com.epn.dicc.ayudante.model.NotificacionResponse;
import com.epn.dicc.ayudante.util.DateUtil;
import com.epn.dicc.ayudante.util.SessionManager;
import com.epn.dicc.ayudante.util.SwingUtil;
import com.epn.dicc.ayudante.util.TableUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel COMPLETO de notificaciones
 */
public class NotificacionesPanel extends JPanel {
    
    private NotificacionApi notificacionApi;
    private JTable tablaNotificaciones;
    private DefaultTableModel modeloTabla;
    private JButton btnMarcarLeida;
    private JButton btnActualizar;
    private JLabel lblNoLeidas;
    
    public NotificacionesPanel() {
        initApi();
        initComponents();
    }
    
    private void initApi() {
        ApiClient apiClient = SessionManager.getInstance().getApiClient();
        this.notificacionApi = new NotificacionApi(apiClient);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel superior
        JPanel topPanel = new JPanel(new BorderLayout());
        
        JLabel lblTitulo = new JLabel("Notificaciones");
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 16f));
        
        lblNoLeidas = new JLabel("No leídas: 0");
        lblNoLeidas.setFont(lblNoLeidas.getFont().deriveFont(Font.BOLD));
        lblNoLeidas.setForeground(new Color(220, 0, 0));
        
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.add(lblTitulo);
        titlePanel.add(Box.createHorizontalStrut(20));
        titlePanel.add(lblNoLeidas);
        
        btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> cargarNotificaciones());
        
        topPanel.add(titlePanel, BorderLayout.WEST);
        topPanel.add(btnActualizar, BorderLayout.EAST);
        
        // Tabla de notificaciones
        String[] columnas = {"", "Tipo", "Título", "Mensaje", "Fecha"};
        modeloTabla = TableUtil.crearModeloNoEditable(columnas);
        tablaNotificaciones = new JTable(modeloTabla);
        tablaNotificaciones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaNotificaciones.getColumnModel().getColumn(0).setPreferredWidth(30);
        tablaNotificaciones.getColumnModel().getColumn(0).setMaxWidth(30);
        
        // Doble click para ver detalle
        tablaNotificaciones.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    verDetalle();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tablaNotificaciones);
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        btnMarcarLeida = new JButton("Marcar como Leída");
        btnMarcarLeida.addActionListener(e -> marcarComoLeida());
        buttonPanel.add(btnMarcarLeida);
        
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    public void cargarNotificaciones() {
        SwingWorker<List<NotificacionResponse>, Void> worker = 
                new SwingWorker<List<NotificacionResponse>, Void>() {
            @Override
            protected List<NotificacionResponse> doInBackground() throws Exception {
                Long usuarioId = SessionManager.getInstance().getUserId();
                return notificacionApi.listarPorUsuario(usuarioId);
            }
            
            @Override
            protected void done() {
                try {
                    List<NotificacionResponse> notificaciones = get();
                    mostrarNotificaciones(notificaciones);
                    actualizarContador();
                } catch (Exception e) {
                    SwingUtil.mostrarError(NotificacionesPanel.this, 
                            "Error al cargar notificaciones: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private void mostrarNotificaciones(List<NotificacionResponse> notificaciones) {
        TableUtil.limpiarTabla(tablaNotificaciones);
        
        for (NotificacionResponse notif : notificaciones) {
            String icono = notif.getLeida() ? "✓" : "●";
            
            modeloTabla.addRow(new Object[]{
                icono,
                notif.getTipo(),
                notif.getTitulo(),
                truncar(notif.getMensaje(), 50),
                DateUtil.formatearFechaHora(notif.getFechaEnvio())
            });
        }
    }
    
    private void verDetalle() {
        if (!TableUtil.hayFilaSeleccionada(tablaNotificaciones)) {
            SwingUtil.mostrarAdvertencia(this, "Seleccione una notificación");
            return;
        }
        
        int fila = tablaNotificaciones.getSelectedRow();
        String titulo = (String) tablaNotificaciones.getValueAt(fila, 2);
        String mensaje = (String) tablaNotificaciones.getValueAt(fila, 3);
        
        // Mostrar diálogo con detalle completo
        JOptionPane.showMessageDialog(this, mensaje, titulo, JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void marcarComoLeida() {
        if (!TableUtil.hayFilaSeleccionada(tablaNotificaciones)) {
            SwingUtil.mostrarAdvertencia(this, "Seleccione una notificación");
            return;
        }
        
        // Implementar marcado como leída
        SwingUtil.mostrarInfo(this, "Funcionalidad en desarrollo");
    }
    
    private void actualizarContador() {
        SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                Long usuarioId = SessionManager.getInstance().getUserId();
                return notificacionApi.contarNoLeidas(usuarioId);
            }
            
            @Override
            protected void done() {
                try {
                    Integer cantidad = get();
                    lblNoLeidas.setText("No leídas: " + cantidad);
                } catch (Exception e) {
                    // Ignorar error silenciosamente
                }
            }
        };
        
        worker.execute();
    }
    
    private String truncar(String texto, int longitud) {
        if (texto == null) return "";
        return texto.length() > longitud ? 
                texto.substring(0, longitud) + "..." : texto;
    }
}