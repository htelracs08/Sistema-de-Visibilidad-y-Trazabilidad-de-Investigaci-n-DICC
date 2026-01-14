package com.epn.dicc.ayudante.view;

import com.epn.dicc.ayudante.api.ApiClient;
import com.epn.dicc.ayudante.api.AuthApi;
import com.epn.dicc.ayudante.model.TokenResponse;
import com.epn.dicc.ayudante.util.SessionManager;
import com.epn.dicc.ayudante.util.SwingUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana de Login
 */
public class LoginFrame extends JFrame {
    
    private JTextField txtCorreo;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnRegistro;
    private AuthApi authApi;
    
    public LoginFrame() {
        initComponents();
        initApi();
    }
    
    private void initApi() {
        ApiClient apiClient = SessionManager.getInstance().getApiClient();
        this.authApi = new AuthApi(apiClient);
        
        // Verificar conexión con el backend
        if (!authApi.verificarConexion()) {
            SwingUtil.mostrarAdvertencia(this, 
                "No se pudo conectar con el servidor.\n" +
                "Asegúrate de que el backend esté ejecutándose en http://localhost:8080");
        }
    }
    
    private void initComponents() {
        setTitle("DICC - Login Ayudante");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setResizable(false);
        
        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Panel de título
        JPanel titlePanel = new JPanel();
        JLabel lblTitle = new JLabel("Sistema DICC - Ayudantes");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        titlePanel.add(lblTitle);
        
        // Panel de formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Correo
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Correo institucional:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtCorreo = new JTextField(20);
        formPanel.add(txtCorreo, gbc);
        
        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Contraseña:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtPassword = new JPasswordField(20);
        formPanel.add(txtPassword, gbc);
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        btnLogin = new JButton("Iniciar Sesión");
        btnLogin.setPreferredSize(new Dimension(130, 30));
        btnLogin.addActionListener(e -> login());
        
        btnRegistro = new JButton("Registrarse");
        btnRegistro.setPreferredSize(new Dimension(130, 30));
        btnRegistro.addActionListener(e -> abrirRegistro());
        
        buttonPanel.add(btnLogin);
        buttonPanel.add(btnRegistro);
        
        // Enter para login
        txtPassword.addActionListener(e -> login());
        
        // Agregar paneles al frame
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        SwingUtil.centrarVentana(this);
        
        // Focus en el campo de correo
        SwingUtilities.invokeLater(() -> txtCorreo.requestFocus());
    }
    
    private void login() {
        String correo = txtCorreo.getText().trim();
        String password = new String(txtPassword.getPassword());
        
        // Validaciones
        if (correo.isEmpty()) {
            SwingUtil.mostrarAdvertencia(this, "Ingrese su correo institucional");
            txtCorreo.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            SwingUtil.mostrarAdvertencia(this, "Ingrese su contraseña");
            txtPassword.requestFocus();
            return;
        }
        
        // Deshabilitar botones mientras se procesa
        btnLogin.setEnabled(false);
        btnRegistro.setEnabled(false);
        
        // Ejecutar login en segundo plano
        SwingWorker<TokenResponse, Void> worker = new SwingWorker<TokenResponse, Void>() {
            @Override
            protected TokenResponse doInBackground() throws Exception {
                return authApi.login(correo, password);
            }
            
            @Override
            protected void done() {
                btnLogin.setEnabled(true);
                btnRegistro.setEnabled(true);
                
                try {
                    TokenResponse token = get();
                    
                    // Guardar sesión
                    SessionManager.getInstance().login(token);
                    
                    SwingUtil.mostrarInfo(LoginFrame.this, 
                            "Bienvenido, " + token.getNombreCompleto());
                    
                    // Abrir ventana principal
                    abrirVentanaPrincipal();
                    
                } catch (Exception e) {
                    String mensaje = e.getCause() != null ? 
                            e.getCause().getMessage() : e.getMessage();
                    SwingUtil.mostrarError(LoginFrame.this, 
                            "Error al iniciar sesión: " + mensaje);
                    txtPassword.setText("");
                    txtPassword.requestFocus();
                }
            }
        };
        
        worker.execute();
    }
    
    private void abrirRegistro() {
        RegistroFrame registroFrame = new RegistroFrame(this);
        registroFrame.setVisible(true);
        this.setVisible(false);
    }
    
    private void abrirVentanaPrincipal() {
        MainFrame mainFrame = new MainFrame();
        mainFrame.setVisible(true);
        this.dispose();
    }
}