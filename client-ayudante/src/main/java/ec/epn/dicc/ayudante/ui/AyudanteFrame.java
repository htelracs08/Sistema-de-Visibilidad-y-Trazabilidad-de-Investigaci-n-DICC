package ec.epn.dicc.ayudante.ui;

import com.google.gson.*;
import ec.epn.dicc.ayudante.api.ApiClient;
import ec.epn.dicc.ayudante.ui.dialogs.NuevaActividadDialog;
import ec.epn.dicc.ayudante.ui.dialogs.NuevaSemanaDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AyudanteFrame extends JFrame {
  private final ApiClient api;

  private final JLabel lblBitacora = new JLabel("Bitácora actual: -");
  private final JLabel lblEstado = new JLabel(" ");

  private String bitacoraIdActual = null;

  private final DefaultListModel<JsonObject> semanasModel = new DefaultListModel<>();
  private final JList<JsonObject> lstSemanas = new JList<>(semanasModel);

  private final DefaultListModel<JsonObject> actividadesModel = new DefaultListModel<>();
  private final JList<JsonObject> lstActividades = new JList<>(actividadesModel);

  public AyudanteFrame(ApiClient api) {
    super("Ayudante - Bitácoras");
    this.api = api;

    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setSize(980, 640);
    setLocationRelativeTo(null);

    JPanel root = new JPanel(new BorderLayout());
    root.setBorder(new EmptyBorder(12, 12, 12, 12));

    // Top
    JPanel top = new JPanel(new BorderLayout(10, 10));
    lblBitacora.setFont(lblBitacora.getFont().deriveFont(Font.BOLD, 14f));
    top.add(lblBitacora, BorderLayout.WEST);

    JPanel topBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton btnActual = new JButton("Obtener Bitácora Actual");
    JButton btnRefrescar = new JButton("Refrescar");
    JButton btnEnviar = new JButton("Enviar Bitácora");

    btnActual.addActionListener(e -> obtenerActual());
    btnRefrescar.addActionListener(e -> refrescar());
    btnEnviar.addActionListener(e -> enviar());

    topBtns.add(btnActual);
    topBtns.add(btnRefrescar);
    topBtns.add(btnEnviar);

    top.add(topBtns, BorderLayout.EAST);

    // Center: split semanas / actividades
    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    split.setResizeWeight(0.5);

    // left panel: semanas
    JPanel left = new JPanel(new BorderLayout(8, 8));
    left.add(new JLabel("Semanas"), BorderLayout.NORTH);

    lstSemanas.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
      String fi = value.has("fechaInicioSemana") ? value.get("fechaInicioSemana").getAsString() : "";
      String ff = value.has("fechaFinSemana") ? value.get("fechaFinSemana").getAsString() : "";
      String id = value.has("semanaId") ? value.get("semanaId").getAsString() : "";
      String txt = fi + " → " + ff + "   (" + id.substring(0, Math.min(8, id.length())) + "...)";
      JLabel lbl = new JLabel(txt);
      lbl.setOpaque(true);
      if (isSelected) lbl.setBackground(new Color(220, 235, 255));
      lbl.setBorder(new EmptyBorder(6, 6, 6, 6));
      return lbl;
    });

    lstSemanas.addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting()) cargarActividadesDeSeleccion();
    });

    left.add(new JScrollPane(lstSemanas), BorderLayout.CENTER);

    JPanel leftBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton btnNuevaSemana = new JButton("Nueva Semana");
    btnNuevaSemana.addActionListener(e -> nuevaSemana());
    leftBtns.add(btnNuevaSemana);
    left.add(leftBtns, BorderLayout.SOUTH);

    // right panel: actividades
    JPanel right = new JPanel(new BorderLayout(8, 8));
    right.add(new JLabel("Actividades (de la semana seleccionada)"), BorderLayout.NORTH);

    lstActividades.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
      String hi = value.has("horaInicio") ? value.get("horaInicio").getAsString() : "";
      String hs = value.has("horaSalida") ? value.get("horaSalida").getAsString() : "";
      String desc = value.has("descripcion") ? value.get("descripcion").getAsString() : "";
      String txt = hi + " - " + hs + " | " + desc;
      JLabel lbl = new JLabel(txt);
      lbl.setOpaque(true);
      if (isSelected) lbl.setBackground(new Color(220, 235, 255));
      lbl.setBorder(new EmptyBorder(6, 6, 6, 6));
      return lbl;
    });

    right.add(new JScrollPane(lstActividades), BorderLayout.CENTER);

    JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton btnNuevaAct = new JButton("Nueva Actividad");
    btnNuevaAct.addActionListener(e -> nuevaActividad());
    rightBtns.add(btnNuevaAct);
    right.add(rightBtns, BorderLayout.SOUTH);

    split.setLeftComponent(left);
    split.setRightComponent(right);

    // Bottom status
    lblEstado.setForeground(new Color(120, 0, 0));

    root.add(top, BorderLayout.NORTH);
    root.add(split, BorderLayout.CENTER);
    root.add(lblEstado, BorderLayout.SOUTH);

    setContentPane(root);

    // auto
    obtenerActual();
  }

  private void setOk(String msg) {
    lblEstado.setForeground(new Color(0, 120, 0));
    lblEstado.setText(msg);
  }

  private void setErr(String msg) {
    lblEstado.setForeground(new Color(160, 0, 0));
    lblEstado.setText(msg);
  }

  private void obtenerActual() {
    setOk("Obteniendo bitácora actual...");
    JsonObject resp = api.obtenerBitacoraActual();
    int code = resp.get("_httpStatus").getAsInt();
    JsonObject data = resp.getAsJsonObject("data");

    if (code != 200 || !data.has("ok") || !data.get("ok").getAsBoolean()) {
      setErr("Error obtener actual. HTTP " + code + " - " + (data.has("msg") ? data.get("msg").getAsString() : ""));
      return;
    }

    bitacoraIdActual = data.get("bitacoraId").getAsString();
    lblBitacora.setText("Bitácora actual: " + bitacoraIdActual);
    refrescar();
  }

  private void refrescar() {
    if (bitacoraIdActual == null) {
      setErr("Primero obtiene la bitácora actual.");
      return;
    }

    JsonObject resp = api.verBitacora(bitacoraIdActual);
    int code = resp.get("_httpStatus").getAsInt();
    JsonObject data = resp.getAsJsonObject("data");

    if (code != 200 || !data.has("ok") || !data.get("ok").getAsBoolean()) {
      setErr("Error ver bitácora. HTTP " + code + " - " + (data.has("msg") ? data.get("msg").getAsString() : ""));
      return;
    }

    // cabecera
    JsonObject bit = data.getAsJsonObject("bitacora");
    String estado = bit.has("estado") ? bit.get("estado").getAsString() : "-";
    lblBitacora.setText("Bitácora actual: " + bitacoraIdActual + " | Estado: " + estado);

    // semanas
    semanasModel.clear();
    actividadesModel.clear();

    JsonArray semanas = data.getAsJsonArray("semanas");
    for (JsonElement el : semanas) semanasModel.addElement(el.getAsJsonObject());

    setOk("Cargado. Semanas: " + semanas.size());
  }

  private void nuevaSemana() {
    if (bitacoraIdActual == null) {
      setErr("Primero obtiene la bitácora actual.");
      return;
    }

    NuevaSemanaDialog dlg = new NuevaSemanaDialog(this);
    dlg.setVisible(true);
    if (!dlg.isOk()) return;

    JsonObject resp = api.crearSemana(
        bitacoraIdActual,
        dlg.getFechaInicio(),
        dlg.getFechaFin(),
        dlg.getActividadesRealizadas(),
        dlg.getObservaciones(),
        dlg.getAnexos()
    );

    int code = resp.get("_httpStatus").getAsInt();
    JsonObject data = resp.getAsJsonObject("data");

    if (code != 200 || !data.has("ok") || !data.get("ok").getAsBoolean()) {
      setErr("Error crear semana. HTTP " + code + " - " + (data.has("msg") ? data.get("msg").getAsString() : ""));
      return;
    }

    setOk("Semana creada: " + data.get("semanaId").getAsString());
    refrescar();
  }

  private void nuevaActividad() {
    JsonObject semana = lstSemanas.getSelectedValue();
    if (semana == null) {
      setErr("Selecciona una semana primero.");
      return;
    }
    String semanaId = semana.get("semanaId").getAsString();

    NuevaActividadDialog dlg = new NuevaActividadDialog(this);
    dlg.setVisible(true);
    if (!dlg.isOk()) return;

    JsonObject resp = api.crearActividad(
        semanaId,
        dlg.getHoraInicio(),
        dlg.getHoraSalida(),
        dlg.getDescripcion()
    );

    int code = resp.get("_httpStatus").getAsInt();
    JsonObject data = resp.getAsJsonObject("data");

    if (code != 200 || !data.has("ok") || !data.get("ok").getAsBoolean()) {
      setErr("Error crear actividad. HTTP " + code + " - " + (data.has("msg") ? data.get("msg").getAsString() : ""));
      return;
    }

    setOk("Actividad creada: " + data.get("actividadId").getAsString());
    refrescar();
  }

  private void cargarActividadesDeSeleccion() {
    actividadesModel.clear();
    JsonObject semana = lstSemanas.getSelectedValue();
    if (semana == null) return;

    // En tu backend, "verBitacora" ya incluye actividades embebidas por semana
    if (semana.has("actividades") && semana.get("actividades").isJsonArray()) {
      JsonArray acts = semana.getAsJsonArray("actividades");
      for (JsonElement el : acts) actividadesModel.addElement(el.getAsJsonObject());
    }
  }

  private void enviar() {
    if (bitacoraIdActual == null) {
      setErr("Primero obtiene la bitácora actual.");
      return;
    }

    int r = JOptionPane.showConfirmDialog(
        this,
        "¿Enviar la bitácora actual para revisión?\n(Después ya no podrás editar si tu backend bloquea ENVIADA)",
        "Confirmar envío",
        JOptionPane.YES_NO_OPTION
    );
    if (r != JOptionPane.YES_OPTION) return;

    JsonObject resp = api.enviarBitacora(bitacoraIdActual);
    int code = resp.get("_httpStatus").getAsInt();
    JsonObject data = resp.getAsJsonObject("data");

    if (code != 200 || !data.has("ok") || !data.get("ok").getAsBoolean()) {
      setErr("Error enviar. HTTP " + code + " - " + (data.has("msg") ? data.get("msg").getAsString() : ""));
      return;
    }

    setOk("Bitácora enviada.");
    refrescar();
  }
}
