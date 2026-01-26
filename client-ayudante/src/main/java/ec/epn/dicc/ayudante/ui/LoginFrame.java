package ec.epn.dicc.ayudante.ui;

import com.google.gson.JsonObject;
import ec.epn.dicc.ayudante.api.ApiClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {
  private final ApiClient api;

  private final JTextField txtCorreo = new JTextField();
  private final JPasswordField txtPass = new JPasswordField();
  private final JLabel lblEstado = new JLabel(" ");

  public LoginFrame(String baseUrl) {
    super("Ayudante - Login");
    this.api = new ApiClient(baseUrl);

    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setSize(520, 260);
    setLocationRelativeTo(null);

    JPanel root = new JPanel(new BorderLayout());
    root.setBorder(new EmptyBorder(16, 16, 16, 16));

    JLabel title = new JLabel("Ingreso Ayudante");
    title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

    JPanel form = new JPanel(new GridLayout(2, 2, 10, 10));
    form.add(new JLabel("Correo:"));
    form.add(txtCorreo);
    form.add(new JLabel("Password:"));
    form.add(txtPass);

    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton btnLogin = new JButton("Ingresar");
    btnLogin.addActionListener(e -> login());
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

    String correo = txtCorreo.getText().trim().toLowerCase();
    String pass = new String(txtPass.getPassword());

    api.setAuth(correo, pass);

    JsonObject resp = api.getMe();
    int code = resp.get("_httpStatus").getAsInt();

    if (code != 200) {
      lblEstado.setText("Login falló. HTTP " + code);
      return;
    }

    JsonObject data = resp.getAsJsonObject("data");
    String rol = data.has("rol") ? data.get("rol").getAsString() : "";

    if (!"AYUDANTE".equalsIgnoreCase(rol)) {
      lblEstado.setText("No autorizado. Rol=" + rol);
      return;
    }

    dispose();
    new AyudanteFrame(api).setVisible(true);
  }
}
