package ec.epn.jefatura;

import ec.epn.jefatura.api.ApiClient;
import ec.epn.jefatura.ui.LoginFrame;

import javax.swing.*;

public class MainJefatura {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      ApiClient api = new ApiClient("http://localhost:8080");
      new LoginFrame(api).setVisible(true);
    });
  }
}