package com.epn.dicc.ayudante.view;

import com.epn.dicc.ayudante.api.ApiClient;
import com.epn.dicc.ayudante.api.BitacoraApi;
import com.epn.dicc.ayudante.model.ActividadResponse;
import com.epn.dicc.ayudante.model.AgregarActividadRequest;
import com.epn.dicc.ayudante.model.BitacoraResponse;
import com.epn.dicc.ayudante.util.DateUtil;
import com.epn.dicc.ayudante.util.SessionManager;
import com.epn.dicc.ayudante.util.SwingUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Diálogo COMPLETO para ver y editar bitácora con conexión API
 */
public class DetalleBitacoraDialog extends JDialog {
    
    private BitacoraResponse bitacora;
    private BitacoraApi bitacoraApi;
    private boolean modificada = false;
    
    private JTable tablaActividades;
    private DefaultTableModel modeloTabla;
    private JButton btnAgregar;
    private JButton btnEliminar;
    private JButton btnEnviar;
    private JButton btnCerrar;
    private JLabel lblEstado;
    private JLabel lblHorasTotales;
    
    public DetalleBitacoraDialog(Frame parent, BitacoraResponse bitacora) {
        super(parent, "Detalle de Bitácora", true);
        this.bitacora = bitacora;
        initApi();
        initComponents();
        cargarDatos();
    }
    
    private void initApi() {
        ApiClient apiClient = SessionManager.getInstance().getApiClient();
        this.bitacoraApi = new BitacoraApi(apiClient);
    }
    
    private void initComponents() {
        setSize(900, 600);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Panel superior - Información
        JPanel infoPanel = createInfoPanel();
        
        // Panel central - Tabla de actividades
        JPanel actividadesPanel = createActividadesPanel();
        
        // Panel inferior - Botones
        JPanel buttonPanel = createButtonPanel();
        
        mainPanel.add(infoPanel, BorderLayout.NORTH);
        mainPanel.add(actividadesPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        SwingUtil.centrarVentana(this);
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Información de la Bitácora"));
        
        panel.add(new JLabel("Código:"));
        panel.add(new JLabel(bitacora.getCodigoBitacora()));
        
        panel.add(new JLabel("Mes/Año:"));
        panel.add(new JLabel(DateUtil.getNombreMes(bitacora.getMes()) + " " + bitacora.getAnio()));
        
        panel.add(new JLabel("Estado:"));
        lblEstado = new JLabel(bitacora.getEstado().toString());
        lblEstado.setFont(lblEstado.getFont().deriveFont(Font.BOLD));
        panel.add(lblEstado);
        
        return panel;
    }
    
    private JPanel createActividadesPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Actividades"));
        
        // Tabla
        String[] columnas = {"#", "Descripción", "Horas", "Fecha", "Categoría"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaActividades = new JTable(modeloTabla);
        tablaActividades.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(tablaActividades);
        
        // Panel de control
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        btnAgregar = new JButton("Agregar Actividad");
        btnAgregar.addActionListener(e -> agregarActividad());
        btnAgregar.setEnabled(bitacora.getPuedeEditar());
        
        btnEliminar = new JButton("Eliminar");
        btnEliminar.addActionListener(e -> eliminarActividad());
        btnEliminar.setEnabled(bitacora.getPuedeEditar());
        
        lblHorasTotales = new JLabel("Horas Totales: " + bitacora.getHorasTotales());
        lblHorasTotales.setFont(lblHorasTotales.getFont().deriveFont(Font.BOLD));
        
        controlPanel.add(btnAgregar);
        controlPanel.add(btnEliminar);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(lblHorasTotales);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(controlPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        btnEnviar = new JButton("Enviar a Revisión");
        btnEnviar.addActionListener(e -> enviarRevision());
        btnEnviar.setEnabled(bitacora.getPuedeEditar());
        
        btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        
        panel.add(btnEnviar);
        panel.add(btnCerrar);
        
        return panel;
    }
    
    private void cargarDatos() {
        modeloTabla.setRowCount(0);
        
        if (bitacora.getActividades() != null) {
            for (ActividadResponse actividad : bitacora.getActividades()) {
                modeloTabla.addRow(new Object[]{
                    actividad.getNumeroActividad(),
                    actividad.getDescripcion(),
                    actividad.getTiempoDedicadoHoras(),
                    DateUtil.formatearFecha(actividad.getFechaEjecucion()),
                    actividad.getCategoria()
                });
            }
        }
    }
    
    private void agregarActividad() {
        AgregarActividadDialog dialog = new AgregarActividadDialog(this, bitacora.getId());
        dialog.setVisible(true);
        
        if (dialog.isAgregada()) {
            modificada = true;
            recargarBitacora();
        }
    }
    
    private void eliminarActividad() {
        int fila = tablaActividades.getSelectedRow();
        if (fila == -1) {
            SwingUtil.mostrarAdvertencia(this, "Seleccione una actividad");
            return;
        }
        
        if (!SwingUtil.confirmar(this, "¿Está seguro de eliminar esta actividad?")) {
            return;
        }
        
        // Obtener ID de la actividad
        Long actividadId = bitacora.getActividades().get(fila).getId();
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                bitacoraApi.eliminarActividad(actividadId);
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    get();
                    modificada = true;
                    SwingUtil.mostrarInfo(DetalleBitacoraDialog.this, "Actividad eliminada");
                    recargarBitacora();
                } catch (Exception e) {
                    SwingUtil.mostrarError(DetalleBitacoraDialog.this, 
                            "Error: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private void enviarRevision() {
        if (!SwingUtil.confirmar(this, 
                "¿Está seguro de enviar esta bitácora a revisión?\n" +
                "Una vez enviada, no podrá modificarla hasta que el director la revise.")) {
            return;
        }
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                bitacoraApi.enviarRevision(bitacora.getId());
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    get();
                    SwingUtil.mostrarInfo(DetalleBitacoraDialog.this, 
                            "Bitácora enviada a revisión exitosamente");
                    modificada = true;
                    dispose();
                } catch (Exception e) {
                    SwingUtil.mostrarError(DetalleBitacoraDialog.this, 
                            "Error: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private void recargarBitacora() {
        SwingWorker<BitacoraResponse, Void> worker = new SwingWorker<BitacoraResponse, Void>() {
            @Override
            protected BitacoraResponse doInBackground() throws Exception {
                return bitacoraApi.obtenerPorId(bitacora.getId());
            }
            
            @Override
            protected void done() {
                try {
                    bitacora = get();
                    lblEstado.setText(bitacora.getEstado().toString());
                    lblHorasTotales.setText("Horas Totales: " + bitacora.getHorasTotales());
                    cargarDatos();
                    
                    btnAgregar.setEnabled(bitacora.getPuedeEditar());
                    btnEliminar.setEnabled(bitacora.getPuedeEditar());
                    btnEnviar.setEnabled(bitacora.getPuedeEditar());
                } catch (Exception e) {
                    SwingUtil.mostrarError(DetalleBitacoraDialog.this, 
                            "Error al recargar: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    public boolean isModificada() {
        return modificada;
    }
}