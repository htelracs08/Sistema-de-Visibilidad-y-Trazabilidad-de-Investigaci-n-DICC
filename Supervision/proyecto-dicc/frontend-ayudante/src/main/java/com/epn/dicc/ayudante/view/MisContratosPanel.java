package com.epn.dicc.ayudante.view;

import com.epn.dicc.ayudante.api.ApiClient;
import com.epn.dicc.ayudante.api.ContratoApi;
import com.epn.dicc.ayudante.model.ContratoResponse;
import com.epn.dicc.ayudante.util.DateUtil;
import com.epn.dicc.ayudante.util.SessionManager;
import com.epn.dicc.ayudante.util.SwingUtil;
import com.epn.dicc.ayudante.util.TableUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel para mostrar contratos del ayudante
 */
public class MisContratosPanel extends JPanel {
    
    private JTable tablaContratos;
    private DefaultTableModel modeloTabla;
    private ContratoApi contratoApi;
    
    public MisContratosPanel() {
        initComponents();
        initApi();
    }
    
    private void initApi() {
        ApiClient apiClient = SessionManager.getInstance().getApiClient();
        this.contratoApi = new ContratoApi(apiClient);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel superior con título y botón
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel lblTitulo = new JLabel("Mis Contratos");
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 16f));
        topPanel.add(lblTitulo, BorderLayout.WEST);
        
        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> cargarContratos());
        topPanel.add(btnActualizar, BorderLayout.EAST);
        
        // Tabla de contratos
        String[] columnas = {
            "Número", "Proyecto", "Estado", "Fecha Solicitud", 
            "Fecha Inicio", "Meses Pactados", "Semestre"
        };
        modeloTabla = TableUtil.crearModeloNoEditable(columnas);
        tablaContratos = new JTable(modeloTabla);
        tablaContratos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Doble click para ver detalles
        tablaContratos.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && tablaContratos.getSelectedRow() != -1) {
                    verDetalles();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tablaContratos);
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton btnVerDetalles = new JButton("Ver Detalles");
        btnVerDetalles.addActionListener(e -> verDetalles());
        buttonPanel.add(btnVerDetalles);
        
        JButton btnRenunciar = new JButton("Renunciar");
        btnRenunciar.addActionListener(e -> renunciar());
        buttonPanel.add(btnRenunciar);
        
        // Agregar componentes
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    public void cargarContratos() {
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
                    mostrarContratos(contratos);
                } catch (Exception e) {
                    SwingUtil.mostrarError(MisContratosPanel.this, 
                            "Error al cargar contratos: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private void mostrarContratos(List<ContratoResponse> contratos) {
        TableUtil.limpiarTabla(tablaContratos);
        
        for (ContratoResponse contrato : contratos) {
            modeloTabla.addRow(new Object[]{
                contrato.getNumeroContrato(),
                contrato.getTituloProyecto(),
                contrato.getEstado(),
                DateUtil.formatearFechaHora(contrato.getFechaSolicitud()),
                DateUtil.formatearFecha(contrato.getFechaInicioContrato()),
                contrato.getMesesPactados(),
                contrato.getSemestreAsignado()
            });
        }
    }
    
    private void verDetalles() {
        if (!TableUtil.hayFilaSeleccionada(tablaContratos)) {
            SwingUtil.mostrarAdvertencia(this, "Seleccione un contrato");
            return;
        }
        
        int fila = tablaContratos.getSelectedRow();
        String numeroContrato = (String) tablaContratos.getValueAt(fila, 0);
        
        // Buscar el contrato en la lista cargada
        SwingWorker<ContratoResponse, Void> worker = new SwingWorker<ContratoResponse, Void>() {
            @Override
            protected ContratoResponse doInBackground() throws Exception {
                Long ayudanteId = SessionManager.getInstance().getUserId();
                List<ContratoResponse> contratos = contratoApi.listarPorAyudante(ayudanteId);
                return contratos.stream()
                        .filter(c -> c.getNumeroContrato().equals(numeroContrato))
                        .findFirst()
                        .orElse(null);
            }
            
            @Override
            protected void done() {
                try {
                    ContratoResponse contrato = get();
                    if (contrato != null) {
                        DetalleContratoDialog dialog = new DetalleContratoDialog(
                                (Frame) SwingUtilities.getWindowAncestor(MisContratosPanel.this),
                                contrato
                        );
                        dialog.setVisible(true);
                    }
                } catch (Exception e) {
                    SwingUtil.mostrarError(MisContratosPanel.this, 
                            "Error: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private void renunciar() {
        if (!TableUtil.hayFilaSeleccionada(tablaContratos)) {
            SwingUtil.mostrarAdvertencia(this, "Seleccione un contrato");
            return;
        }
        
        if (SwingUtil.confirmar(this, 
                "¿Está seguro que desea renunciar a este contrato?\n" +
                "Esta acción no se puede deshacer.")) {
            // Implementar lógica de renuncia
            SwingUtil.mostrarInfo(this, "Función en desarrollo");
        }
    }
}