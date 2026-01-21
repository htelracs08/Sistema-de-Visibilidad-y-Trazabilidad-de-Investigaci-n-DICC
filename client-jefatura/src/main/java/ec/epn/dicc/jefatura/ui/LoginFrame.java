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

    setContentPane(p);
  }

  private void doLogin() {
    btnLogin.setEnabled(false);
    lblStatus.setText("Validando...");

    SwingWorker<Void, Void> worker = new SwingWorker<>() {
      @Override protected Void doInBackground() throws Exception {
        String correo = txtCorreo.getText().trim().toLowerCase();
        String pass = new String(txtPass.getPassword());

        api.setBasicAuth(correo, pass);
        String body = api.get(Endpoints.ME);

        JsonObject me = JsonParser.parseString(body).getAsJsonObject();
        String rol = me.has("rol") ? me.get("rol").getAsString() : "";

        if (!"JEFATURA".equalsIgnoreCase(rol)) {
          throw new RuntimeException("No autorizado. Rol=" + rol);
        }
        return null;
      }

      @Override protected void done() {
        try {
          get();
          lblStatus.setText("OK");
          dispose();
          new MainFrame(api).setVisible(true);
        } catch (Exception ex) {
          lblStatus.setText("Error");
          JOptionPane.showMessageDialog(LoginFrame.this,
              ex.getMessage(), "Login falló", JOptionPane.ERROR_MESSAGE);
          btnLogin.setEnabled(true);
        }
      }
    };

    worker.execute();
  }
}
