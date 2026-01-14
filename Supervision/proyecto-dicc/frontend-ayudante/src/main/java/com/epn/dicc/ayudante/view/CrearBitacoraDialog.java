package com.epn.dicc.ayudante.view;

import com.epn.dicc.ayudante.api.ApiClient;
import com.epn.dicc.ayudante.api.BitacoraApi;
import com.epn.dicc.ayudante.model.ContratoResponse;
import com.epn.dicc.ayudante.model.CrearBitacoraRequest;
import com.epn.dicc.ayudante.util.SessionManager;
import com.epn.dicc.ayudante.util.SwingUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Diálogo COMPLETO para crear nueva bitácora con conexión API
 */
public class CrearBitacoraDialog extends JDialog {
    
    private ContratoResponse contrato;
    private BitacoraApi bitacoraApi;
    private boolean creada = false;
    
    private JComboBox<String> cboMes;
    private JSpinner spnAnio;
    private JTextArea txtComentarios;
    private JButton btnCrear;
    private JButton btnCancelar;
    
    public CrearBitacoraDialog(Frame parent, ContratoResponse contrato) {
        super(parent, "Crear Nueva Bitácora", true);
        this.contrato = contrato;
        initApi();
        initComponents();
    }
    
    private void initApi() {
        ApiClient apiClient = SessionManager.getInstance().getApiClient();
        this.bitacoraApi = new BitacoraApi(apiClient);
    }
    
    private void initComponents() {
        setSize(450, 350);
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Título
        JPanel titlePanel = new JPanel();
        JLabel lblTitle = new JLabel("Crear Bitácora Mensual");
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 14f));
        titlePanel.add(lblTitle);
        
        // Info del contrato
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Información del Contrato"));
        JTextArea txtInfo = new JTextArea(3, 30);
        txtInfo.setEditable(false);
        txtInfo.setText(
                "Proyecto: " + contrato.getTituloProyecto() + "\n" +
                "Contrato: " + contrato.getNumeroContrato() + "\n" +
                "Semestre: " + contrato.getSemestreAsignado()
        );
        infoPanel.add(new JScrollPane(txtInfo), BorderLayout.CENTER);
        
        // Formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Datos de la Bitácora"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Mes
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        formPanel.add(new JLabel("Mes:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                         "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        cboMes = new JComboBox<>(meses);
        cboMes.setSelectedIndex(java.time.LocalDate.now().getMonthValue() - 1);
        formPanel.add(cboMes, gbc);
        
        // Año
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Año:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        int anioActual = java.time.LocalDate.now().getYear();
        spnAnio = new JSpinner(new SpinnerNumberModel(anioActual, 2020, 2030, 1));
        formPanel.add(spnAnio, gbc);
        
        // Comentarios
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        formPanel.add(new JLabel("Comentarios (opcional):"), gbc);
        
        gbc.gridy = 3;
        txtComentarios = new JTextArea(4, 30);
        txtComentarios.setLineWrap(true);
        txtComentarios.setWrapStyleWord(true);
        JScrollPane scrollComent = new JScrollPane(txtComentarios);
        formPanel.add(scrollComent, gbc);
        
        // Botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        btnCrear = new JButton("Crear Bitácora");
        btnCrear.addActionListener(e -> crearBitacora());
        
        btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());
        
        buttonPanel.add(btnCrear);
        buttonPanel.add(btnCancelar);
        
        // Ensamblar
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.add(infoPanel, BorderLayout.NORTH);
        contentPanel.add(formPanel, BorderLayout.CENTER);
        
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        SwingUtil.centrarVentana(this);
    }
    
    private void crearBitacora() {
        // Crear request
        CrearBitacoraRequest request = new CrearBitacoraRequest();
        request.setContratoId(contrato.getId());
        request.setMes(cboMes.getSelectedIndex() + 1);
        request.setAnio((Integer) spnAnio.getValue());
        request.setComentariosAyudante(txtComentarios.getText().trim());
        
        btnCrear.setEnabled(false);
        btnCancelar.setEnabled(false);
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                bitacoraApi.crear(request);
                return null;
            }
            
            @Override
            protected void done() {
                btnCrear.setEnabled(true);
                btnCancelar.setEnabled(true);
                
                try {
                    get();
                    SwingUtil.mostrarInfo(CrearBitacoraDialog.this, 
                            "Bitácora creada exitosamente");
                    creada = true;
                    dispose();
                } catch (Exception e) {
                    String mensaje = e.getCause() != null ? 
                            e.getCause().getMessage() : e.getMessage();
                    SwingUtil.mostrarError(CrearBitacoraDialog.this, 
                            "Error al crear bitácora: " + mensaje);
                }
            }
        };
        
        worker.execute();
    }
    
    public boolean isCreada() {
        return creada;
    }
}