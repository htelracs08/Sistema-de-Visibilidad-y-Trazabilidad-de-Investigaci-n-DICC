package ec.epn.dicc.jefatura;

import javax.swing.*;

import ec.epn.dicc.jefatura.api.ApiClient;
import ec.epn.dicc.jefatura.ui.LoginFrame;

public class MainJefatura {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      ApiClient api = new ApiClient("http://localhost:8080");
      new LoginFrame(api).setVisible(true);
    });
  }
}