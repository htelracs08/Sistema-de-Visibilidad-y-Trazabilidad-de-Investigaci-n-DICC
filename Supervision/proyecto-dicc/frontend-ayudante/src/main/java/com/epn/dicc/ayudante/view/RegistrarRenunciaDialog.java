package com.epn.dicc.ayudante.view;

import com.epn.dicc.ayudante.api.ApiClient;
import com.epn.dicc.ayudante.api.ContratoApi;
import com.epn.dicc.ayudante.model.ContratoResponse;
import com.epn.dicc.ayudante.util.SessionManager;
import com.epn.dicc.ayudante.util.SwingUtil;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.awt.*;
import java.time.ZoneId;

/**
 * Diálogo para registrar renuncia a un contrato
 */
public class RegistrarRenunciaDialog extends JDialog {
    
    private ContratoResponse contrato;
    private ContratoApi contratoApi;
    private boolean registrada = false;
    
    private JDateChooser dateChooser;
    private JTextArea txtMotivo;
    private JButton btnRegistrar;
    private JButton btnCancelar;
    
    public RegistrarRenunciaDialog(Frame parent, ContratoResponse contrato) {
        super(parent, "Registrar Renuncia", true);
        this.contrato = contrato;
        initApi();
        initComponents();
    }
    
    private void initApi() {
        ApiClient apiClient = SessionManager.getInstance().getApiClient();
        this.contratoApi = new ContratoApi(apiClient);
    }
    
    private void initComponents() {
        setSize(450, 350);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Advertencia
        JPanel warningPanel = new JPanel(new BorderLayout());
        warningPanel.setBackground(new Color(255, 240, 200));
        warningPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel lblWarning = new JLabel("<html><b>⚠ ADVERTENCIA:</b><br>" +
                "Esta acción es irreversible. Una vez registrada la renuncia,<br>" +
                "no podrá seguir trabajando en este proyecto.</html>");
        warningPanel.add(lblWarning, BorderLayout.CENTER);
        
        // Formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Proyecto
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblProyecto = new JLabel("Proyecto: " + contrato.getTituloProyecto());
        lblProyecto.setFont(lblProyecto.getFont().deriveFont(Font.BOLD));
        formPanel.add(lblProyecto, gbc);
        
        // Fecha de renuncia
        gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Fecha de renuncia:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        dateChooser = new JDateChooser();
        dateChooser.setDate(new java.util.Date());
        formPanel.add(dateChooser, gbc);
        
        // Motivo
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        formPanel.add(new JLabel("Motivo de la renuncia:"), gbc);
        
        gbc.gridy = 3;
        txtMotivo = new JTextArea(5, 30);
        txtMotivo.setLineWrap(true);
        txtMotivo.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(txtMotivo), gbc);
        
        // Botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        btnRegistrar = new JButton("Registrar Renuncia");
        btnRegistrar.addActionListener(e -> registrarRenuncia());
        
        btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());
        
        buttonPanel.add(btnRegistrar);
        buttonPanel.add(btnCancelar);
        
        mainPanel.add(warningPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        SwingUtil.centrarVentana(this);
    }
    
    private void registrarRenuncia() {
        if (txtMotivo.getText().trim().isEmpty()) {
            SwingUtil.mostrarAdvertencia(this, "Debe ingresar el motivo de la renuncia");
            return;
        }
        
        if (!SwingUtil.confirmar(this, 
                "¿Está seguro de registrar su renuncia?\n" +
                "Esta acción NO se puede deshacer.")) {
            return;
        }
        
        btnRegistrar.setEnabled(false);
        btnCancelar.setEnabled(false);
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                java.time.LocalDate fecha = dateChooser.getDate().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate();
                contratoApi.registrarRenuncia(contrato.getId(), fecha, txtMotivo.getText().trim());
                return null;
            }
            
            @Override
            protected void done() {
                btnRegistrar.setEnabled(true);
                btnCancelar.setEnabled(true);
                
                try {
                    get();
                    SwingUtil.mostrarInfo(RegistrarRenunciaDialog.this, 
                            "Renuncia registrada exitosamente");
                    registrada = true;
                    dispose();
                } catch (Exception e) {
                    SwingUtil.mostrarError(RegistrarRenunciaDialog.this, 
                            "Error: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    public boolean isRegistrada() {
        return registrada;
    }
}