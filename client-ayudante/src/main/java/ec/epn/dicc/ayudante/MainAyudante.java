package ec.epn.dicc.ayudante;

import ec.epn.dicc.ayudante.ui.LoginFrame;
import ec.epn.dicc.ayudante.util.Props;

import javax.swing.*;

public class MainAyudante {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      String baseUrl = Props.get("api.baseUrl", "http://localhost:8080/api/v1");
      new LoginFrame(baseUrl).setVisible(true);
    });
  }
}
