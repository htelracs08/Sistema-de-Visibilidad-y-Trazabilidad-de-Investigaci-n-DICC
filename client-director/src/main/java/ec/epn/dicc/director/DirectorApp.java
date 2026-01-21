package ec.epn.dicc.director;

import ec.epn.dicc.director.ui.LoginFrame;

import javax.swing.*;

public class DirectorApp {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception ignored) {}
      new LoginFrame("http://localhost:8080").setVisible(true);
    });
  }
}
