package ec.epn.dicc.director.ui;

import com.google.gson.JsonObject;
import ec.epn.dicc.director.api.ApiClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {

  private final ApiClient api;

  private final JTextField txtCorreo = new JTextField();
  private final JPasswordField txtPass = new JPasswordField();
  private final JLabel lblEstado = new JLabel(" ");

  public LoginFrame(String baseUrl) {
    super("Director - Login");
    this.api = new ApiClient(baseUrl);

    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setSize(520, 260);
    setLocationRelativeTo(null);

    JPanel root = new JPanel(new BorderLayout());
    root.setBorder(new EmptyBorder(16, 16, 16, 16));

    JLabel title = new JLabel("Ingreso Director");
    title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

    JPanel form = new JPanel(new GridLayout(2, 2, 10, 10));
    form.add(new JLabel("Correo:"));
    form.add(txtCorreo);
    form.add(new JLabel("Password:"));
    form.add(txtPass);

    JButton btnLogin = new JButton("Ingresar");
    btnLogin.addActionListener(e -> login());

    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    actions.add(btnLogin);

    lblEstado.setForeground(new Color(160, 0, 0));

    root.add(title, BorderLayout.NORTH);
    root.add(form, BorderLayout.CENTER);

    JPanel bottom = new JPanel(new BorderLayout());
    bottom.add(lblEstado, BorderLayout.CENTER);
    bottom.add(actions, BorderLayout.EAST);
    root.add(bottom, BorderLayout.SOUTH);

    setContentPane(root);

    // Defaults para pruebas rápidas (puedes borrar)
    txtCorreo.setText("ariel.guana@epn.edu.ec");
    txtPass.setText("Director2026*");
  }

  private void login() {
    lblEstado.setText("Validando...");

    String correo = txtCorreo.getText().trim().toLowerCase();
    String pass = new String(txtPass.getPassword());

    if (correo.isBlank() || pass.isBlank()) {
      lblEstado.setText("Correo y contraseña son requeridos");
      return;
    }

    try {
      api.setAuth(correo, pass);

      JsonObject resp = api.getMe();

      // HTTP status (inyectado por ApiClient)
      int code = resp.has("_httpStatus")
          ? resp.get("_httpStatus").getAsInt()
          : 500;

      if (code != 200) {
        lblEstado.setText("Login falló. HTTP " + code);
        return;
      }

      // Validar ok
      boolean ok = resp.has("ok") && resp.get("ok").getAsBoolean();
      if (!ok) {
        lblEstado.setText("Login falló");
        return;
      }

      // 🔑 LEER ROL DIRECTAMENTE (NO data)
      String rol = resp.has("rol") ? resp.get("rol").getAsString() : "";
      rol = rol == null ? "" : rol.trim().toUpperCase();

      if (!"DIRECTOR".equals(rol)) {
        lblEstado.setText("No autorizado. Rol=" + rol);
        return;
      }

      // Todo OK → entrar al sistema
      dispose();
      new DirectorFrame(api).setVisible(true);

    } catch (Exception ex) {
      lblEstado.setText("Error: " + ex.getMessage());
      ex.printStackTrace();
    }
  }
}
