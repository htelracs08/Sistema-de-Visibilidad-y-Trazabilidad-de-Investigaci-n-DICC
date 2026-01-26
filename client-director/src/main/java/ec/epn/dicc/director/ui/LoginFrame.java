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

    // Permitir login con Enter
    txtPass.addActionListener(e -> login());

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
    clearForm();
  }

  private void clearForm() {
    txtCorreo.setText("");
    txtPass.setText("");
    lblEstado.setText(" ");
  }

  @Override
  public void setVisible(boolean b) {
    if (b) clearForm();
    super.setVisible(b);
  }

  private void login() {
    lblEstado.setText("Validando...");
    lblEstado.setForeground(new Color(0, 0, 180));

    String correo = txtCorreo.getText().trim().toLowerCase();
    String pass = new String(txtPass.getPassword());

    if (correo.isBlank() || pass.isBlank()) {
      lblEstado.setText("Correo y contraseña son requeridos");
      lblEstado.setForeground(new Color(180, 0, 0));
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
        lblEstado.setForeground(new Color(180, 0, 0));
        
        String errorMsg = "Login falló. HTTP " + code;
        if (resp.has("error")) {
          errorMsg += "\n" + resp.get("error").getAsString();
        }
        JOptionPane.showMessageDialog(this, errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      // Acceder a los datos directamente desde resp.get("data")
      JsonObject data = resp.has("data") && resp.get("data").isJsonObject() 
          ? resp.get("data").getAsJsonObject() 
          : null;

      if (data == null) {
        lblEstado.setText("Respuesta inválida del servidor");
        lblEstado.setForeground(new Color(180, 0, 0));
        JOptionPane.showMessageDialog(this, "El servidor no retornó datos válidos", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      // Validar ok
      boolean ok = data.has("ok") && data.get("ok").getAsBoolean();
      if (!ok) {
        String msg = data.has("msg") ? data.get("msg").getAsString() : "Login falló";
        lblEstado.setText(msg);
        lblEstado.setForeground(new Color(180, 0, 0));
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      // Leer rol
      String rol = data.has("rol") && !data.get("rol").isJsonNull() 
          ? data.get("rol").getAsString() 
          : "";
      rol = rol.trim().toUpperCase();

      if (!"DIRECTOR".equals(rol)) {
        lblEstado.setText("No autorizado. Rol=" + rol);
        lblEstado.setForeground(new Color(180, 0, 0));
        JOptionPane.showMessageDialog(this, 
            "Este cliente es solo para usuarios con rol DIRECTOR.\nTu rol: " + rol, 
            "Acceso Denegado", JOptionPane.WARNING_MESSAGE);
        return;
      }

      // Todo OK → entrar al sistema
      lblEstado.setText("Login exitoso");
      lblEstado.setForeground(new Color(0, 120, 0));
      
      dispose();
      new DirectorFrame(api).setVisible(true);

    } catch (Exception ex) {
      lblEstado.setText("Error: " + ex.getMessage());
      lblEstado.setForeground(new Color(180, 0, 0));
      ex.printStackTrace();
      JOptionPane.showMessageDialog(this, 
          "Error al conectar con el servidor:\n" + ex.getMessage(), 
          "Error de Conexión", JOptionPane.ERROR_MESSAGE);
    }
  }
}