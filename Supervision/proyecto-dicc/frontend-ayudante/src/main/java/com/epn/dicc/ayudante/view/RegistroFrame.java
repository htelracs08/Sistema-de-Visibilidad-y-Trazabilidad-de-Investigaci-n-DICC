package com.epn.dicc.ayudante.view;

import com.epn.dicc.ayudante.api.ApiClient;
import com.epn.dicc.ayudante.api.AuthApi;
import com.epn.dicc.ayudante.model.RegistroRequest;
import com.epn.dicc.ayudante.model.UsuarioResponse;
import com.epn.dicc.ayudante.util.SwingUtil;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Ventana de Registro para Ayudantes
 */
public class RegistroFrame extends JFrame {
    
    private JFrame parentFrame;
    private AuthApi authApi;
    
    // Campos del formulario
    private JTextField txtCodigoEPN;
    private JTextField txtCedula;
    private JTextField txtNombres;
    private JTextField txtApellidos;
    private JTextField txtCorreo;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmarPassword;
    private JTextField txtCarrera;
    private JTextField txtFacultad;
    private JComboBox<Integer> cboQuintil;
    private JSpinner spnSemestre;
    private JTextField txtPromedio;
    
    private JButton btnRegistrar;
    private JButton btnCancelar;
    
    public RegistroFrame(JFrame parent) {
        this.parentFrame = parent;
        initApi();
        initComponents();
    }
    
    private void initApi() {
        ApiClient apiClient = new ApiClient();
        this.authApi = new AuthApi(apiClient);
    }
    
    private void initComponents() {
        setTitle("Registro de Ayudante");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 650);
        setResizable(false);
        
        // Panel principal con scroll
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Título
        JPanel titlePanel = new JPanel();
        JLabel lblTitle = new JLabel("Registro de Nuevo Ayudante");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        titlePanel.add(lblTitle);
        
        // Formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        
        // Código EPN
        addFormField(formPanel, gbc, row++, "Código EPN:", 
                txtCodigoEPN = new JTextField(20), 
                "Ejemplo: L00123456");
        
        // Cédula
        addFormField(formPanel, gbc, row++, "Cédula:", 
                txtCedula = new JTextField(20), 
                "10 dígitos");
        
        // Nombres
        addFormField(formPanel, gbc, row++, "Nombres:", 
                txtNombres = new JTextField(20), null);
        
        // Apellidos
        addFormField(formPanel, gbc, row++, "Apellidos:", 
                txtApellidos = new JTextField(20), null);
        
        // Correo institucional
        addFormField(formPanel, gbc, row++, "Correo institucional:", 
                txtCorreo = new JTextField(20), 
                "debe terminar en @epn.edu.ec");
        
        // Contraseña
        addFormField(formPanel, gbc, row++, "Contraseña:", 
                txtPassword = new JPasswordField(20), 
                "Mínimo 6 caracteres");
        
        // Confirmar contraseña
        addFormField(formPanel, gbc, row++, "Confirmar contraseña:", 
                txtConfirmarPassword = new JPasswordField(20), null);
        
        // Carrera
        addFormField(formPanel, gbc, row++, "Carrera:", 
                txtCarrera = new JTextField(20), 
                "Ejemplo: Ingeniería en Sistemas");
        
        // Facultad
        addFormField(formPanel, gbc, row++, "Facultad:", 
                txtFacultad = new JTextField(20), 
                "Ejemplo: Facultad de Ingeniería en Sistemas");
        
        // Quintil
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Quintil:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        cboQuintil = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        formPanel.add(cboQuintil, gbc);
        row++;
        
        // Semestre actual
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Semestre actual:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        spnSemestre = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
        formPanel.add(spnSemestre, gbc);
        row++;
        
        // Promedio
        addFormField(formPanel, gbc, row++, "Promedio general:", 
                txtPromedio = new JTextField(20), 
                "Ejemplo: 8.5");
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        btnRegistrar = new JButton("Registrarse");
        btnRegistrar.setPreferredSize(new Dimension(120, 30));
        btnRegistrar.addActionListener(e -> registrar());
        
        btnCancelar = new JButton("Cancelar");
        btnCancelar.setPreferredSize(new Dimension(120, 30));
        btnCancelar.addActionListener(e -> cancelar());
        
        buttonPanel.add(btnRegistrar);
        buttonPanel.add(btnCancelar);
        
        // Agregar componentes
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        SwingUtil.centrarVentana(this);
    }
    
    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, 
                              String label, JComponent field, String hint) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        
        if (hint != null) {
            JPanel fieldPanel = new JPanel(new BorderLayout());
            fieldPanel.add(field, BorderLayout.NORTH);
            JLabel hintLabel = new JLabel(hint);
            hintLabel.setFont(hintLabel.getFont().deriveFont(Font.ITALIC, 10f));
            hintLabel.setForeground(Color.GRAY);
            fieldPanel.add(hintLabel, BorderLayout.SOUTH);
            panel.add(fieldPanel, gbc);
        } else {
            panel.add(field, gbc);
        }
    }
    
    private void registrar() {
        // Validaciones
        if (!validarCampos()) {
            return;
        }
        
        // Crear request
        RegistroRequest request = new RegistroRequest();
        request.setCodigoEPN(txtCodigoEPN.getText().trim());
        request.setCedula(txtCedula.getText().trim());
        request.setNombres(txtNombres.getText().trim());
        request.setApellidos(txtApellidos.getText().trim());
        request.setCorreoInstitucional(txtCorreo.getText().trim());
        request.setPassword(new String(txtPassword.getPassword()));
        request.setCarrera(txtCarrera.getText().trim());
        request.setFacultad(txtFacultad.getText().trim());
        request.setQuintil((Integer) cboQuintil.getSelectedItem());
        request.setSemestreActual((Integer) spnSemestre.getValue());
        
        try {
            request.setPromedioGeneral(new BigDecimal(txtPromedio.getText().trim()));
        } catch (NumberFormatException e) {
            SwingUtil.mostrarError(this, "El promedio debe ser un número válido");
            return;
        }
        
        // Deshabilitar botones
        btnRegistrar.setEnabled(false);
        btnCancelar.setEnabled(false);
        
        // Registrar en segundo plano
        SwingWorker<UsuarioResponse, Void> worker = new SwingWorker<UsuarioResponse, Void>() {
            @Override
            protected UsuarioResponse doInBackground() throws Exception {
                return authApi.registrar(request);
            }
            
            @Override
            protected void done() {
                btnRegistrar.setEnabled(true);
                btnCancelar.setEnabled(true);
                
                try {
                    UsuarioResponse usuario = get();
                    
                    SwingUtil.mostrarInfo(RegistroFrame.this, 
                            "¡Registro exitoso!\n\nYa puedes iniciar sesión con tu correo y contraseña.");
                    
                    volverAlLogin();
                    
                } catch (Exception e) {
                    String mensaje = e.getCause() != null ? 
                            e.getCause().getMessage() : e.getMessage();
                    SwingUtil.mostrarError(RegistroFrame.this, 
                            "Error al registrarse: " + mensaje);
                }
            }
        };
        
        worker.execute();
    }
    
    private boolean validarCampos() {
        // Validar campos vacíos
        if (txtCodigoEPN.getText().trim().isEmpty()) {
            SwingUtil.mostrarAdvertencia(this, "Ingrese su código EPN");
            txtCodigoEPN.requestFocus();
            return false;
        }
        
        if (txtCedula.getText().trim().length() != 10) {
            SwingUtil.mostrarAdvertencia(this, "La cédula debe tener 10 dígitos");
            txtCedula.requestFocus();
            return false;
        }
        
        if (txtNombres.getText().trim().isEmpty()) {
            SwingUtil.mostrarAdvertencia(this, "Ingrese sus nombres");
            txtNombres.requestFocus();
            return false;
        }
        
        if (txtApellidos.getText().trim().isEmpty()) {
            SwingUtil.mostrarAdvertencia(this, "Ingrese sus apellidos");
            txtApellidos.requestFocus();
            return false;
        }
        
        String correo = txtCorreo.getText().trim();
        if (!correo.toLowerCase().endsWith("@epn.edu.ec")) {
            SwingUtil.mostrarAdvertencia(this, "Debe usar un correo institucional @epn.edu.ec");
            txtCorreo.requestFocus();
            return false;
        }
        
        String password = new String(txtPassword.getPassword());
        if (password.length() < 6) {
            SwingUtil.mostrarAdvertencia(this, "La contraseña debe tener al menos 6 caracteres");
            txtPassword.requestFocus();
            return false;
        }
        
        String confirmar = new String(txtConfirmarPassword.getPassword());
        if (!password.equals(confirmar)) {
            SwingUtil.mostrarAdvertencia(this, "Las contraseñas no coinciden");
            txtConfirmarPassword.requestFocus();
            return false;
        }
        
        if (txtCarrera.getText().trim().isEmpty()) {
            SwingUtil.mostrarAdvertencia(this, "Ingrese su carrera");
            txtCarrera.requestFocus();
            return false;
        }
        
        if (txtFacultad.getText().trim().isEmpty()) {
            SwingUtil.mostrarAdvertencia(this, "Ingrese su facultad");
            txtFacultad.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void cancelar() {
        volverAlLogin();
    }
    
    private void volverAlLogin() {
        parentFrame.setVisible(true);
        this.dispose();
    }
}