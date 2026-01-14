package com.epn.dicc.ayudante.view;

import com.epn.dicc.ayudante.api.ApiClient;
import com.epn.dicc.ayudante.api.BitacoraApi;
import com.epn.dicc.ayudante.model.AgregarActividadRequest;
import com.epn.dicc.ayudante.util.SessionManager;
import com.epn.dicc.ayudante.util.SwingUtil;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.ZoneId;

/**
 * Diálogo para agregar actividad a bitácora
 */
public class AgregarActividadDialog extends JDialog {
    
    private Long bitacoraId;
    private BitacoraApi bitacoraApi;
    private boolean agregada = false;
    
    private JTextArea txtDescripcion;
    private JTextArea txtObjetivo;
    private JTextArea txtResultado;
    private JTextField txtHoras;
    private JDateChooser dateChooser;
    private JTextField txtCategoria;
    private JButton btnAgregar;
    private JButton btnCancelar;
    
    public AgregarActividadDialog(Dialog parent, Long bitacoraId) {
        super(parent, "Agregar Actividad", true);
        this.bitacoraId = bitacoraId;
        initApi();
        initComponents();
    }
    
    private void initApi() {
        ApiClient apiClient = SessionManager.getInstance().getApiClient();
        this.bitacoraApi = new BitacoraApi(apiClient);
    }
    
    private void initComponents() {
        setSize(500, 550);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        
        // Descripción
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        formPanel.add(new JLabel("Descripción de la actividad:"), gbc);
        
        gbc.gridy = row++;
        txtDescripcion = new JTextArea(3, 30);
        txtDescripcion.setLineWrap(true);
        formPanel.add(new JScrollPane(txtDescripcion), gbc);
        
        // Objetivo
        gbc.gridy = row++;
        formPanel.add(new JLabel("Objetivo:"), gbc);
        
        gbc.gridy = row++;
        txtObjetivo = new JTextArea(2, 30);
        txtObjetivo.setLineWrap(true);
        formPanel.add(new JScrollPane(txtObjetivo), gbc);
        
        // Resultado
        gbc.gridy = row++;
        formPanel.add(new JLabel("Resultado obtenido:"), gbc);
        
        gbc.gridy = row++;
        txtResultado = new JTextArea(2, 30);
        txtResultado.setLineWrap(true);
        formPanel.add(new JScrollPane(txtResultado), gbc);
        
        // Horas
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Horas dedicadas:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        txtHoras = new JTextField();
        formPanel.add(txtHoras, gbc);
        
        row++;
        
        // Fecha
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Fecha de ejecución:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        dateChooser = new JDateChooser();
        dateChooser.setDate(new java.util.Date());
        formPanel.add(dateChooser, gbc);
        
        row++;
        
        // Categoría
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Categoría:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        txtCategoria = new JTextField();
        formPanel.add(txtCategoria, gbc);
        
        // Botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        btnAgregar = new JButton("Agregar");
        btnAgregar.addActionListener(e -> agregar());
        
        btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());
        
        buttonPanel.add(btnAgregar);
        buttonPanel.add(btnCancelar);
        
        mainPanel.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        SwingUtil.centrarVentana(this);
    }
    
    private void agregar() {
        // Validaciones
        if (txtDescripcion.getText().trim().isEmpty()) {
            SwingUtil.mostrarAdvertencia(this, "Ingrese la descripción");
            return;
        }
        
        BigDecimal horas;
        try {
            horas = new BigDecimal(txtHoras.getText().trim());
            if (horas.compareTo(BigDecimal.ZERO) <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            SwingUtil.mostrarAdvertencia(this, "Ingrese un número válido de horas");
            return;
        }
        
        // Crear request
        AgregarActividadRequest request = new AgregarActividadRequest();
        request.setBitacoraId(bitacoraId);
        request.setDescripcion(txtDescripcion.getText().trim());
        request.setObjetivoActividad(txtObjetivo.getText().trim());
        request.setResultadoObtenido(txtResultado.getText().trim());
        request.setTiempoDedicadoHoras(horas);
        request.setFechaEjecucion(dateChooser.getDate().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate());
        request.setCategoria(txtCategoria.getText().trim());
        
        btnAgregar.setEnabled(false);
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                bitacoraApi.agregarActividad(request);
                return null;
            }
            
            @Override
            protected void done() {
                btnAgregar.setEnabled(true);
                try {
                    get();
                    SwingUtil.mostrarInfo(AgregarActividadDialog.this, 
                            "Actividad agregada exitosamente");
                    agregada = true;
                    dispose();
                } catch (Exception e) {
                    SwingUtil.mostrarError(AgregarActividadDialog.this, 
                            "Error: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    public boolean isAgregada() {
        return agregada;
    }
}