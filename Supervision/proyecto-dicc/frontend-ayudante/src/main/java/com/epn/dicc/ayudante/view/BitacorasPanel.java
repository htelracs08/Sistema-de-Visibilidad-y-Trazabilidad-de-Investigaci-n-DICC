package com.epn.dicc.ayudante.view;

import com.epn.dicc.ayudante.api.ApiClient;
import com.epn.dicc.ayudante.api.BitacoraApi;
import com.epn.dicc.ayudante.api.ContratoApi;
import com.epn.dicc.ayudante.model.BitacoraResponse;
import com.epn.dicc.ayudante.model.ContratoResponse;
import com.epn.dicc.ayudante.util.DateUtil;
import com.epn.dicc.ayudante.util.SessionManager;
import com.epn.dicc.ayudante.util.SwingUtil;
import com.epn.dicc.ayudante.util.TableUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel completo de Bitácoras Mensuales
 */
public class BitacorasPanel extends JPanel {
    
    private JComboBox<ContratoItem> cboContratos;
    private JTable tablaBitacoras;
    private DefaultTableModel modeloTabla;
    private JButton btnNuevaBitacora;
    private JButton btnVerDetalle;
    private JButton btnEnviar;
    private JButton btnActualizar;
    
    private ContratoApi contratoApi;
    private BitacoraApi bitacoraApi;
    private List<ContratoResponse> contratosActivos;
    
    public BitacorasPanel() {
        initApis();
        initComponents();
        cargarContratos();
    }
    
    private void initApis() {
        ApiClient apiClient = SessionManager.getInstance().getApiClient();
        this.contratoApi = new ContratoApi(apiClient);
        this.bitacoraApi = new BitacoraApi(apiClient);
        this.contratosActivos = new ArrayList<>();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel superior
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        
        // Título
        JLabel lblTitulo = new JLabel("Bitácoras Mensuales");
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 16f));
        
        // Panel de selección de contrato
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectionPanel.add(new JLabel("Contrato:"));
        cboContratos = new JComboBox<>();
        cboContratos.setPreferredSize(new Dimension(400, 25));
        cboContratos.addActionListener(e -> cargarBitacoras());
        selectionPanel.add(cboContratos);
        
        topPanel.add(lblTitulo, BorderLayout.NORTH);
        topPanel.add(selectionPanel, BorderLayout.CENTER);
        
        // Tabla de bitácoras
        String[] columnas = {
            "Código", "Mes/Año", "Estado", "Horas Totales", 
            "Fecha Envío", "Actividades"
        };
        modeloTabla = TableUtil.crearModeloNoEditable(columnas);
        tablaBitacoras = new JTable(modeloTabla);
        tablaBitacoras.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Doble click para ver detalle
        tablaBitacoras.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && tablaBitacoras.getSelectedRow() != -1) {
                    verDetalle();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tablaBitacoras);
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        
        btnNuevaBitacora = new JButton("Nueva Bitácora");
        btnNuevaBitacora.addActionListener(e -> nuevaBitacora());
        
        btnVerDetalle = new JButton("Ver Detalle");
        btnVerDetalle.addActionListener(e -> verDetalle());
        
        btnEnviar = new JButton("Enviar a Revisión");
        btnEnviar.addActionListener(e -> enviarRevision());
        
        btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> actualizarDatos());
        
        buttonPanel.add(btnNuevaBitacora);
        buttonPanel.add(btnVerDetalle);
        buttonPanel.add(btnEnviar);
        buttonPanel.add(btnActualizar);
        
        // Agregar componentes
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void cargarContratos() {
        SwingWorker<List<ContratoResponse>, Void> worker = 
                new SwingWorker<List<ContratoResponse>, Void>() {
            @Override
            protected List<ContratoResponse> doInBackground() throws Exception {
                Long ayudanteId = SessionManager.getInstance().getUserId();
                return contratoApi.listarPorAyudante(ayudanteId);
            }
            
            @Override
            protected void done() {
                try {
                    List<ContratoResponse> contratos = get();
                    
                    // Filtrar solo contratos activos
                    contratosActivos = contratos.stream()
                            .filter(c -> "ACTIVO".equals(c.getEstado()))
                            .toList();
                    
                    cboContratos.removeAllItems();
                    
                    if (contratosActivos.isEmpty()) {
                        cboContratos.addItem(new ContratoItem(null, "No tienes contratos activos"));
                        btnNuevaBitacora.setEnabled(false);
                    } else {
                        for (ContratoResponse contrato : contratosActivos) {
                            String texto = contrato.getTituloProyecto() + " - " + 
                                         contrato.getNumeroContrato();
                            cboContratos.addItem(new ContratoItem(contrato, texto));
                        }
                        btnNuevaBitacora.setEnabled(true);
                        cargarBitacoras();
                    }
                    
                } catch (Exception e) {
                    SwingUtil.mostrarError(BitacorasPanel.this, 
                            "Error al cargar contratos: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private void cargarBitacoras() {
        ContratoItem item = (ContratoItem) cboContratos.getSelectedItem();
        if (item == null || item.getContrato() == null) {
            TableUtil.limpiarTabla(tablaBitacoras);
            return;
        }
        
        Long contratoId = item.getContrato().getId();
        
        SwingWorker<List<BitacoraResponse>, Void> worker = 
                new SwingWorker<List<BitacoraResponse>, Void>() {
            @Override
            protected List<BitacoraResponse> doInBackground() throws Exception {
                return bitacoraApi.listarPorContrato(contratoId);
            }
            
            @Override
            protected void done() {
                try {
                    List<BitacoraResponse> bitacoras = get();
                    mostrarBitacoras(bitacoras);
                } catch (Exception e) {
                    SwingUtil.mostrarError(BitacorasPanel.this, 
                            "Error al cargar bitácoras: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private void mostrarBitacoras(List<BitacoraResponse> bitacoras) {
        TableUtil.limpiarTabla(tablaBitacoras);
        
        for (BitacoraResponse bitacora : bitacoras) {
            String mesAnio = DateUtil.getNombreMes(bitacora.getMes()) + "/" + bitacora.getAnio();
            int numActividades = bitacora.getActividades() != null ? 
                    bitacora.getActividades().size() : 0;
            
            modeloTabla.addRow(new Object[]{
                bitacora.getCodigoBitacora(),
                mesAnio,
                bitacora.getEstado(),
                bitacora.getHorasTotales(),
                DateUtil.formatearFechaHora(bitacora.getFechaEnvio()),
                numActividades
            });
        }
    }
    
    private void nuevaBitacora() {
        ContratoItem item = (ContratoItem) cboContratos.getSelectedItem();
        if (item == null || item.getContrato() == null) {
            SwingUtil.mostrarAdvertencia(this, "Seleccione un contrato");
            return;
        }
        
        CrearBitacoraDialog dialog = new CrearBitacoraDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                item.getContrato()
        );
        dialog.setVisible(true);
        
        if (dialog.isCreada()) {
            cargarBitacoras();
        }
    }
    
    private void verDetalle() {
        if (!TableUtil.hayFilaSeleccionada(tablaBitacoras)) {
            SwingUtil.mostrarAdvertencia(this, "Seleccione una bitácora");
            return;
        }
        
        int fila = tablaBitacoras.getSelectedRow();
        String codigoBitacora = (String) tablaBitacoras.getValueAt(fila, 0);
        
        // Buscar la bitácora completa
        ContratoItem item = (ContratoItem) cboContratos.getSelectedItem();
        if (item == null || item.getContrato() == null) return;
        
        SwingWorker<List<BitacoraResponse>, Void> worker = 
                new SwingWorker<List<BitacoraResponse>, Void>() {
            @Override
            protected List<BitacoraResponse> doInBackground() throws Exception {
                return bitacoraApi.listarPorContrato(item.getContrato().getId());
            }
            
            @Override
            protected void done() {
                try {
                    List<BitacoraResponse> bitacoras = get();
                    BitacoraResponse bitacora = bitacoras.stream()
                            .filter(b -> b.getCodigoBitacora().equals(codigoBitacora))
                            .findFirst()
                            .orElse(null);
                    
                    if (bitacora != null) {
                        DetalleBitacoraDialog dialog = new DetalleBitacoraDialog(
                                (Frame) SwingUtilities.getWindowAncestor(BitacorasPanel.this),
                                bitacora
                        );
                        dialog.setVisible(true);
                        
                        if (dialog.isModificada()) {
                            cargarBitacoras();
                        }
                    }
                } catch (Exception e) {
                    SwingUtil.mostrarError(BitacorasPanel.this, 
                            "Error: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private void enviarRevision() {
        if (!TableUtil.hayFilaSeleccionada(tablaBitacoras)) {
            SwingUtil.mostrarAdvertencia(this, "Seleccione una bitácora");
            return;
        }
        
        if (SwingUtil.confirmar(this, 
                "¿Está seguro que desea enviar esta bitácora a revisión?\n" +
                "Una vez enviada, no podrá modificarla hasta que el director la revise.")) {
            
            // Implementar envío
            SwingUtil.mostrarInfo(this, "Funcionalidad en desarrollo");
        }
    }
    
    public void actualizarDatos() {
        cargarContratos();
    }
    
    // Clase auxiliar para el ComboBox
    private static class ContratoItem {
        private ContratoResponse contrato;
        private String texto;
        
        public ContratoItem(ContratoResponse contrato, String texto) {
            this.contrato = contrato;
            this.texto = texto;
        }
        
        public ContratoResponse getContrato() {
            return contrato;
        }
        
        @Override
        public String toString() {
            return texto;
        }
    }
}