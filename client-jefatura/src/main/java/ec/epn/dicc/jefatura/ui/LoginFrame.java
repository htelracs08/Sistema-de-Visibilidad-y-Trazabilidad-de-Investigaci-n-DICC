package ec.epn.dicc.jefatura.ui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ec.epn.dicc.jefatura.api.ApiClient;
import ec.epn.dicc.jefatura.api.Endpoints;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

  private final ApiClient api;

  private final JTextField txtCorreo = new JTextField();
  private final JPasswordField txtPass = new JPasswordField();
  private final JButton btnLogin = new JButton("Ingresar");
  private final JLabel lblStatus = new JLabel(" ");

  public LoginFrame(ApiClient api) {
    super("Jefatura - Login");
    this.api = api;

    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setSize(420, 220);
    setLocationRelativeTo(null);

    JPanel p = new JPanel(new GridLayout(4, 2, 8, 8));
    p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

    p.add(new JLabel("Correo:"));
    p.add(txtCorreo);

    p.add(new JLabel("Password:"));
    p.add(txtPass);

    p.add(new JLabel(""));
    p.add(btnLogin);

    p.add(new JLabel("Estado:"));
    p.add(lblStatus);

    btnLogin.addActionListener(e -> doLogin());
    
    // Permitir login con Enter
    txtPass.addActionListener(e -> doLogin());

    setContentPane(p);
    clearForm();
  }

  private void clearForm() {
    txtCorreo.setText("");
    txtPass.setText("");
    lblStatus.setText(" ");
    btnLogin.setEnabled(true);
  }

  @Override
  public void setVisible(boolean b) {
    if (b) clearForm();
    super.setVisible(b);
  }

  private void doLogin() {
    btnLogin.setEnabled(false);
    lblStatus.setText("Validando...");
    lblStatus.setForeground(Color.BLUE);

    SwingWorker<Void, Void> worker = new SwingWorker<>() {
      String errorMsg = null;
      
      @Override protected Void doInBackground() throws Exception {
        String correo = txtCorreo.getText().trim().toLowerCase();
        String pass = new String(txtPass.getPassword());

        if (correo.isEmpty()) {
          throw new RuntimeException("Correo requerido");
        }
        if (pass.isEmpty()) {
          throw new RuntimeException("Password requerida");
        }

        // Configurar autenticación
        api.setBasicAuth(correo, pass);
        
        // Llamar al endpoint /me
        String body = api.get(Endpoints.ME);
        
        // Parsear respuesta
        JsonObject me = JsonParser.parseString(body).getAsJsonObject();
        
        // Verificar que la respuesta sea exitosa
        if (me.has("ok") && !me.get("ok").getAsBoolean()) {
          String msg = me.has("msg") ? me.get("msg").getAsString() : "Error de autenticación";
          throw new RuntimeException(msg);
        }
        
        // Verificar que tenga el rol
        if (!me.has("rol")) {
          throw new RuntimeException("El servidor no retornó el rol del usuario");
        }
        
        String rol = me.get("rol").getAsString();

        // Validar que sea JEFATURA
        if (!"JEFATURA".equalsIgnoreCase(rol)) {
          throw new RuntimeException("Acceso denegado. Este cliente es solo para JEFATURA. Tu rol: " + rol);
        }
        
        return null;
      }

      @Override protected void done() {
        try {
          get(); // Lanza excepción si hubo error
          
          // Login exitoso
          lblStatus.setText("✓ Login exitoso");
          lblStatus.setForeground(new Color(0, 128, 0));
          
          // Esperar un momento antes de cambiar de ventana
          Timer timer = new Timer(500, e -> {
            dispose();
            new MainFrame(api).setVisible(true);
          });
          timer.setRepeats(false);
          timer.start();
          
        } catch (Exception ex) {
          // Login fallido
          String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
          
          lblStatus.setText("✗ Error");
          lblStatus.setForeground(Color.RED);
          
          JOptionPane.showMessageDialog(
              LoginFrame.this,
              msg,
              "Login Falló",
              JOptionPane.ERROR_MESSAGE
          );
          
          btnLogin.setEnabled(true);
          txtPass.setText(""); // Limpiar password
          txtPass.requestFocus();
        }
      }
    };

    worker.execute();
  }
}