package ec.epn.dicc.jefatura.ui;

import javax.swing.*;

import ec.epn.dicc.jefatura.api.ApiClient;
import ec.epn.dicc.jefatura.ui.panels.*;

public class MainFrame extends JFrame {

  public MainFrame(ApiClient api) {
    super("Sistema DICC - Jefatura");

    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setSize(1100, 700);
    setLocationRelativeTo(null);

    JTabbedPane tabs = new JTabbedPane();

    tabs.addTab("Dashboard", new DashboardPanel(api));
    tabs.addTab("Proyectos", new ProyectosPanel(api));
    tabs.addTab("Semáforo", new SemaforoPanel(api));
    tabs.addTab("Estadísticas", new EstadisticasPanel(api));

    setContentPane(tabs);
  }
}
