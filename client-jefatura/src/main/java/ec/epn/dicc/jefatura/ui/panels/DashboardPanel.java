package ec.epn.dicc.jefatura.ui.panels;

import com.google.gson.*;

import ec.epn.dicc.jefatura.api.ApiClient;
import ec.epn.dicc.jefatura.api.Endpoints;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DashboardPanel extends JPanel {

  private final ApiClient api;

  private final JLabel lblActivos = new JLabel("-");
  private final JLabel lblEstado = new JLabel(" ");

  public DashboardPanel(ApiClient api) {
    this.api = api;
    setLayout(new BorderLayout());
    setBorder(new EmptyBorder(12, 12, 12, 12));

    JPanel top = new JPanel(new BorderLayout(12, 12));
    top.add(buildCards(), BorderLayout.CENTER);
    top.add(buildActions(), BorderLayout.EAST);

    add(top, BorderLayout.NORTH);

    JPanel bottom = new JPanel(new BorderLayout());
    lblEstado.setForeground(new Color(80, 80, 80));
    bottom.add(lblEstado, BorderLayout.WEST);

    add(bottom, BorderLayout.SOUTH);

    refresh();
  }

  private JPanel buildCards() {
    JPanel cards = new JPanel(new GridLayout(1, 1, 12, 12));

    JPanel cardActivos = new JPanel(new BorderLayout());
    cardActivos.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(220, 220, 220)),
        new EmptyBorder(12, 12, 12, 12)
    ));
    JLabel title = new JLabel("Ayudantes activos (global)");
    title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
    lblActivos.setFont(lblActivos.getFont().deriveFont(Font.BOLD, 28f));
    lblActivos.setForeground(new Color(30, 90, 160));

    cardActivos.add(title, BorderLayout.NORTH);
    cardActivos.add(lblActivos, BorderLayout.CENTER);

    cards.add(cardActivos);
    return cards;
  }

  private JPanel buildActions() {
    JPanel actions = new JPanel();
    actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));

    JButton btnRefresh = new JButton("Refrescar");
    btnRefresh.addActionListener(e -> refresh());

    JButton btnMe = new JButton("Ver /me");
    btnMe.addActionListener(e -> showMe());

    actions.add(btnRefresh);
    actions.add(Box.createVerticalStrut(8));
    actions.add(btnMe);

    return actions;
  }

  private void refresh() {
    lblEstado.setText("Cargando...");
    SwingWorker<Void, Void> w = new SwingWorker<>() {
      String err = null;
      int activos = -1;

      @Override protected Void doInBackground() {
        try {
          String json = api.get(Endpoints.AYUDANTES_ACTIVOS);
          JsonObject o = JsonParser.parseString(json).getAsJsonObject();
          activos = o.has("activos") ? o.get("activos").getAsInt() : -1;
        } catch (Exception ex) {
          err = ex.getMessage();
        }
        return null;
      }

      @Override protected void done() {
        if (err != null) {
          lblEstado.setText("Error: " + err);
          JOptionPane.showMessageDialog(DashboardPanel.this, err, "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }
        lblActivos.setText(String.valueOf(activos));
        lblEstado.setText("OK");
      }
    };
    w.execute();
  }

  private void showMe() {
    SwingWorker<Void, Void> w = new SwingWorker<>() {
      String body;
      String err;

      @Override protected Void doInBackground() {
        try {
          body = api.get(Endpoints.ME);
        } catch (Exception e) {
          err = e.getMessage();
        }
        return null;
      }

      @Override protected void done() {
        if (err != null) {
          JOptionPane.showMessageDialog(DashboardPanel.this, err, "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }
        JTextArea ta = new JTextArea(body, 14, 60);
        ta.setEditable(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        JOptionPane.showMessageDialog(DashboardPanel.this, new JScrollPane(ta), "/api/v1/me", JOptionPane.INFORMATION_MESSAGE);
      }
    };
    w.execute();
  }
}
