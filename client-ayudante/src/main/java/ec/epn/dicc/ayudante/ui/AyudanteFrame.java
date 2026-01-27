package ec.epn.dicc.ayudante.ui;

import com.google.gson.*;
import ec.epn.dicc.ayudante.api.ApiClient;
import ec.epn.dicc.ayudante.ui.dialogs.NuevaActividadDialog;
import ec.epn.dicc.ayudante.ui.dialogs.NuevaSemanaDialog;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AyudanteFrame extends JFrame {
  private final ApiClient api;

  private final JLabel lblCreadoInfo = new JLabel("Creada: -");
  private final JLabel lblEstadoInfo = new JLabel("Estado: -");
  private final JLabel lblEstado = new JLabel(" ");

  private String bitacoraIdActual = null;
  private String estadoActual = null;

  private JButton btnActual;
  private JButton btnRefrescar;
  private JButton btnEnviar;
  private JButton btnNuevaBitacora;
  private JButton btnNuevaSemana;
  private JButton btnNuevaAct;
  private JButton btnEditarActividad;
  private JButton btnImprimirAprobadas;

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

    JPanel infoPanel = new JPanel();
    infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
    lblCreadoInfo.setFont(lblCreadoInfo.getFont().deriveFont(Font.BOLD, 14f));
    lblEstadoInfo.setFont(lblEstadoInfo.getFont().deriveFont(Font.BOLD, 14f));
    infoPanel.add(lblCreadoInfo);
    infoPanel.add(Box.createVerticalStrut(4));
    infoPanel.add(lblEstadoInfo);
    top.add(infoPanel, BorderLayout.WEST);

    JPanel actionsPanel = new JPanel();
    actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.Y_AXIS));
    JPanel primaryRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JPanel secondaryRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    btnActual = new JButton("Obtener Bitácora Actual");
    btnRefrescar = new JButton("Refrescar");
    btnEnviar = new JButton("Enviar Bitácora");
    btnNuevaBitacora = new JButton("Agregar nueva bitácora mensual");
    btnImprimirAprobadas = new JButton("Imprimir Aprobadas");
    btnImprimirAprobadas.setEnabled(false);

    btnActual.addActionListener(e -> seleccionarBitacora());
    btnRefrescar.addActionListener(e -> refrescar());
    btnEnviar.addActionListener(e -> enviar());
    btnNuevaBitacora.addActionListener(e -> agregarNuevaBitacoraMensual());
    btnImprimirAprobadas.addActionListener(e -> imprimirAprobadas());

    primaryRow.add(btnActual);
    primaryRow.add(btnNuevaBitacora);

    secondaryRow.add(btnEnviar);
    secondaryRow.add(btnRefrescar);
    secondaryRow.add(btnImprimirAprobadas);

    actionsPanel.add(primaryRow);
    actionsPanel.add(secondaryRow);
    top.add(actionsPanel, BorderLayout.EAST);

    // Center: split semanas / actividades
    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    split.setResizeWeight(0.5);

    // left panel: semanas
    JPanel left = new JPanel(new BorderLayout(8, 8));
    left.add(new JLabel("Semanas"), BorderLayout.NORTH);

    lstSemanas.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
      String fi = value.has("fechaInicioSemana") ? value.get("fechaInicioSemana").getAsString() : "";
      String ff = value.has("fechaFinSemana") ? value.get("fechaFinSemana").getAsString() : "";
      String txt = "Semana " + (index + 1) + ": " + fi + " → " + ff;
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
    btnNuevaSemana = new JButton("Nueva Semana");
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

    lstActividades.addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting()) actualizarEditarActividadState();
    });

    right.add(new JScrollPane(lstActividades), BorderLayout.CENTER);

    JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    btnNuevaAct = new JButton("Nueva Actividad");
    btnEditarActividad = new JButton("Editar Actividad");
    btnEditarActividad.setEnabled(false);
    btnNuevaAct.addActionListener(e -> nuevaActividad());
    btnEditarActividad.addActionListener(e -> editarActividad());
    rightBtns.add(btnNuevaAct);
    rightBtns.add(btnEditarActividad);
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
    seleccionarBitacora();
  }

  private void setOk(String msg) {
    lblEstado.setForeground(new Color(0, 120, 0));
    lblEstado.setText(msg);
  }

  private void setErr(String msg) {
    lblEstado.setForeground(new Color(160, 0, 0));
    lblEstado.setText(msg);
  }

  private void seleccionarBitacora() {
    setOk("Cargando bitácoras...");

    List<BitacoraOption> options = new ArrayList<>();
    int defaultIndex = -1;

    // 1) Bitácoras aprobadas
    JsonObject respAprobadas = api.listarBitacorasAprobadas();
    int codeAprobadas = respAprobadas.get("_httpStatus").getAsInt();
    JsonObject dataAprobadas = respAprobadas.getAsJsonObject("data");

    if (codeAprobadas == 200 && dataAprobadas != null && dataAprobadas.has("ok") && dataAprobadas.get("ok").getAsBoolean()) {
      JsonArray bitacoras = dataAprobadas.has("bitacoras") && dataAprobadas.get("bitacoras").isJsonArray()
          ? dataAprobadas.getAsJsonArray("bitacoras")
          : new JsonArray();
      for (int i = 0; i < bitacoras.size(); i++) {
        JsonObject b = bitacoras.get(i).getAsJsonObject();
        String id = b.has("bitacoraId") ? b.get("bitacoraId").getAsString() : null;
        String creado = b.has("creadoEn") && !b.get("creadoEn").isJsonNull() ? b.get("creadoEn").getAsString() : "-";
        String estado = b.has("estado") ? b.get("estado").getAsString() : "-";
        if (id != null) options.add(new BitacoraOption(id, creado, estado));
      }
    }

    // 2) Bitácora actual (BORRADOR/RECHAZADA/ENVIADA)
    JsonObject respActual = api.obtenerBitacoraActual();
    int codeActual = respActual.get("_httpStatus").getAsInt();
    JsonObject dataActual = respActual.getAsJsonObject("data");
    if (codeActual == 200 && dataActual != null && dataActual.has("ok") && dataActual.get("ok").getAsBoolean()) {
      String actualId = dataActual.get("bitacoraId").getAsString();
      if (actualId != null) {
        JsonObject respBit = api.verBitacora(actualId);
        int codeBit = respBit.get("_httpStatus").getAsInt();
        JsonObject dataBit = respBit.getAsJsonObject("data");
        if (codeBit == 200 && dataBit != null && dataBit.has("ok") && dataBit.get("ok").getAsBoolean()) {
          JsonObject bit = dataBit.getAsJsonObject("bitacora");
          String creado = bit.has("creadoEn") && !bit.get("creadoEn").isJsonNull() ? bit.get("creadoEn").getAsString() : "-";
          String estado = bit.has("estado") ? bit.get("estado").getAsString() : "-";
          String shortId = actualId.length() > 8 ? actualId.substring(0, 8) : actualId;
          setOk("obtenerBitacoraActual -> id=" + shortId + " estado=" + estado);
          boolean exists = options.stream().anyMatch(o -> o.id.equals(actualId));
          if (!exists) {
            options.add(0, new BitacoraOption(actualId, creado, estado));
          }
          if ("BORRADOR".equalsIgnoreCase(estado)) {
            defaultIndex = 0;
          }
        }
      }
    }

    if (options.isEmpty()) {
      setOk("No hay bitácoras para mostrar.");
      JOptionPane.showMessageDialog(this, "No hay bitácoras para mostrar.", "Bitácoras", JOptionPane.INFORMATION_MESSAGE);
      return;
    }

    String[] labels = options.stream().map(BitacoraOption::label).toArray(String[]::new);
    if (defaultIndex < 0) {
      for (int i = 0; i < options.size(); i++) {
        if ("BORRADOR".equalsIgnoreCase(options.get(i).estado)) {
          defaultIndex = i;
          break;
        }
      }
    }
    if (defaultIndex < 0) defaultIndex = 0;
    String selected = (String) JOptionPane.showInputDialog(
        this,
        "Selecciona una bitácora:",
        "Mis Bitácoras",
        JOptionPane.PLAIN_MESSAGE,
        null,
        labels,
      labels[defaultIndex]
    );
    if (selected == null) return;

    BitacoraOption chosen = null;
    for (int i = 0; i < labels.length; i++) {
      if (labels[i].equals(selected)) {
        chosen = options.get(i);
        break;
      }
    }
    if (chosen == null) return;

    bitacoraIdActual = chosen.id;
    estadoActual = chosen.estado;
    lblCreadoInfo.setText("Creada: " + chosen.creadoEn);
    lblEstadoInfo.setText("Estado: " + chosen.estado);
    actualizarEstadoUI(estadoActual);
    refrescar();
  }

  private void agregarNuevaBitacoraMensual() {
    setOk("Nueva bitácora mensual...");
    JsonObject resp = api.obtenerBitacoraActual();
    int code = resp.get("_httpStatus").getAsInt();
    JsonObject data = resp.getAsJsonObject("data");

    if (code != 200 || data == null || !data.has("ok") || !data.get("ok").getAsBoolean()) {
      setErr("Error al crear/obtener bitácora. HTTP " + code + " - " + (data != null && data.has("msg") ? data.get("msg").getAsString() : ""));
      return;
    }

    String newId = data.get("bitacoraId").getAsString();
    boolean misma = newId != null && newId.equals(bitacoraIdActual);
    bitacoraIdActual = newId;
    estadoActual = "BORRADOR";
    semanasModel.clear();
    actividadesModel.clear();

    if (misma) {
      setOk("Ya existe una bitácora en BORRADOR. No se creó otra.");
      JOptionPane.showMessageDialog(this, "Ya existe una bitácora en BORRADOR.", "Bitácora mensual", JOptionPane.INFORMATION_MESSAGE);
    } else {
      setOk("Nueva bitácora mensual.");
      JOptionPane.showMessageDialog(this, "Nueva bitácora mensual.", "Bitácora mensual", JOptionPane.INFORMATION_MESSAGE);
    }

    lblCreadoInfo.setText("Creada: -");
    lblEstadoInfo.setText("Estado: BORRADOR");
    actualizarEstadoUI(estadoActual);
    refrescar();
  }

  private void refrescar() {
    if (bitacoraIdActual == null) {
      setErr("Selecciona una bitácora.");
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
    String creadoEn = bit.has("creadoEn") && !bit.get("creadoEn").isJsonNull()
      ? bit.get("creadoEn").getAsString()
      : "-";
    estadoActual = estado;
    lblCreadoInfo.setText("Creada: " + creadoEn);
    lblEstadoInfo.setText("Estado: " + estado);
    actualizarEstadoUI(estado);

    // semanas
    semanasModel.clear();
    actividadesModel.clear();
    actualizarEditarActividadState();

    JsonArray semanas = data.getAsJsonArray("semanas");
    for (JsonElement el : semanas) semanasModel.addElement(el.getAsJsonObject());

    setOk("Cargado. Semanas: " + semanas.size());
  }

  private void actualizarEstadoUI(String estado) {
    boolean editable = "BORRADOR".equalsIgnoreCase(estado) || "RECHAZADA".equalsIgnoreCase(estado);
    boolean borrador = "BORRADOR".equalsIgnoreCase(estado);
    btnNuevaSemana.setEnabled(borrador);
    btnNuevaAct.setEnabled(editable);
    btnEnviar.setEnabled(editable);
    btnImprimirAprobadas.setEnabled("APROBADA".equalsIgnoreCase(estado));

    if ("RECHAZADA".equalsIgnoreCase(estado)) {
      btnNuevaSemana.setToolTipText("En estado RECHAZADA solo se permiten correcciones.");
    } else {
      btnNuevaSemana.setToolTipText(null);
    }

    if ("RECHAZADA".equalsIgnoreCase(estado)) {
      btnEnviar.setText("Reenviar Bitácora");
    } else {
      btnEnviar.setText("Enviar Bitácora");
    }

    if (!editable) {
      setOk("Bitácora en estado " + estado + ". Solo lectura.");
    }
    actualizarEditarActividadState();
  }

  private void nuevaSemana() {
    if (!"BORRADOR".equalsIgnoreCase(estadoActual) && !"RECHAZADA".equalsIgnoreCase(estadoActual)) {
      setErr("Solo puedes editar si está en BORRADOR o RECHAZADA.");
      return;
    }
    if (bitacoraIdActual == null) {
      setErr("Selecciona una bitácora.");
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
    actualizarEditarActividadState();
  }

  private void nuevaActividad() {
    if (!"BORRADOR".equalsIgnoreCase(estadoActual) && !"RECHAZADA".equalsIgnoreCase(estadoActual)) {
      setErr("Solo puedes editar si está en BORRADOR o RECHAZADA.");
      return;
    }
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
    actualizarEditarActividadState();
  }

  private void actualizarEditarActividadState() {
    boolean editable = "BORRADOR".equalsIgnoreCase(estadoActual) || "RECHAZADA".equalsIgnoreCase(estadoActual);
    boolean selected = lstActividades.getSelectedValue() != null;
    if (btnEditarActividad != null) {
      btnEditarActividad.setEnabled(editable && selected);
    }
  }

  private void editarActividad() {
    if (!"BORRADOR".equalsIgnoreCase(estadoActual) && !"RECHAZADA".equalsIgnoreCase(estadoActual)) {
      setErr("Solo puedes editar si está en BORRADOR o RECHAZADA.");
      return;
    }
    JsonObject actividad = lstActividades.getSelectedValue();
    if (actividad == null) {
      setErr("Selecciona una actividad primero.");
      return;
    }

    JsonObject semana = lstSemanas.getSelectedValue();
    String semanaId = semana != null && semana.has("semanaId") ? semana.get("semanaId").getAsString() : null;

    String actividadId = actividad.has("actividadId") ? actividad.get("actividadId").getAsString() : null;
    if (actividadId == null || actividadId.isBlank()) {
      setErr("Actividad sin ID.");
      return;
    }

    String hi = actividad.has("horaInicio") ? actividad.get("horaInicio").getAsString() : "";
    String hs = actividad.has("horaSalida") ? actividad.get("horaSalida").getAsString() : "";
    String desc = actividad.has("descripcion") ? actividad.get("descripcion").getAsString() : "";

    NuevaActividadDialog dlg = new NuevaActividadDialog(
        this,
        "Editar actividad",
        "Guardar",
        hi,
        hs,
        desc
    );
    dlg.setOnOkValidation(() -> {
      JsonObject resp = api.actualizarActividad(
          actividadId,
          dlg.getHoraInicio(),
          dlg.getHoraSalida(),
          dlg.getDescripcion()
      );

      int code = resp.get("_httpStatus").getAsInt();
      JsonObject data = resp.getAsJsonObject("data");

      if (code != 200 || data == null || !data.has("ok") || !data.get("ok").getAsBoolean()) {
        String msg = data != null && data.has("msg") ? data.get("msg").getAsString() : "";
        return "Error actualizar actividad. HTTP " + code + (msg.isBlank() ? "" : " - " + msg);
      }

      return null;
    });
    dlg.setVisible(true);
    if (!dlg.isOk()) return;

    setOk("Actividad actualizada.");
    refrescar();
    if (semanaId != null) {
      for (int i = 0; i < semanasModel.size(); i++) {
        JsonObject s = semanasModel.getElementAt(i);
        if (s.has("semanaId") && semanaId.equals(s.get("semanaId").getAsString())) {
          lstSemanas.setSelectedIndex(i);
          break;
        }
      }
    }
  }

  private void enviar() {
    if (bitacoraIdActual == null) {
      setErr("Selecciona una bitácora.");
      return;
    }

    if (!"BORRADOR".equalsIgnoreCase(estadoActual) && !"RECHAZADA".equalsIgnoreCase(estadoActual)) {
      setErr("Solo puedes enviar si está en BORRADOR o RECHAZADA.");
      return;
    }

    String accion = "RECHAZADA".equalsIgnoreCase(estadoActual) ? "reenviar" : "enviar";
    int r = JOptionPane.showConfirmDialog(
        this,
        "¿Deseas " + accion + " la bitácora para revisión?\n(Después ya no podrás editar)",
        "Confirmar",
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

  private void imprimirAprobadas() {
    if (bitacoraIdActual == null) {
      setErr("Selecciona una bitácora.");
      return;
    }

    if (!"APROBADA".equalsIgnoreCase(estadoActual)) {
      setErr("Solo se permite imprimir bitácoras APROBADAS.");
      return;
    }

    JsonObject respBit = api.verBitacora(bitacoraIdActual);
    int codeBit = respBit.get("_httpStatus").getAsInt();
    JsonObject dataBit = respBit.getAsJsonObject("data");

    if (codeBit != 200) {
      setErr("No autorizado o error del servidor. HTTP " + codeBit + " - " + (dataBit != null && dataBit.has("msg") ? dataBit.get("msg").getAsString() : ""));
      return;
    }

    if (dataBit == null || !dataBit.has("ok") || !dataBit.get("ok").getAsBoolean()) {
      setErr(dataBit != null && dataBit.has("msg") ? dataBit.get("msg").getAsString() : "No autorizado");
      return;
    }

    JsonObject bit = dataBit.getAsJsonObject("bitacora");
    if (bit == null) {
      setErr("Bitácora no encontrada.");
      return;
    }
    if (dataBit.has("estudiante") && dataBit.get("estudiante").isJsonObject()) {
      bit.add("estudiante", dataBit.getAsJsonObject("estudiante"));
    }
    JsonArray semanas = dataBit.has("semanas") && dataBit.get("semanas").isJsonArray()
        ? dataBit.getAsJsonArray("semanas")
        : new JsonArray();
    exportarPdf(bit, semanas);
  }

  private void exportarPdf(JsonObject bitacora, JsonArray semanas) {
    if (bitacora == null) {
      setErr("No hay datos de bitácora para imprimir.");
      return;
    }
    if (semanas == null) semanas = new JsonArray();

    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Guardar PDF de bitácora");
    chooser.setSelectedFile(new File("bitacora_aprobada.pdf"));
    int r = chooser.showSaveDialog(this);
    if (r != JFileChooser.APPROVE_OPTION) return;

    File file = chooser.getSelectedFile();
    if (!file.getName().toLowerCase().endsWith(".pdf")) {
      file = new File(file.getParentFile(), file.getName() + ".pdf");
    }

    try {
      generarPdfBitacora(bitacora, semanas, file);
      setOk("PDF generado: " + file.getAbsolutePath());
    } catch (IOException e) {
      setErr("Error al generar PDF: " + e.getMessage());
    }
  }

  private void generarPdfBitacora(JsonObject bitacora, JsonArray semanas, File file) throws IOException {
    try (PDDocument doc = new PDDocument()) {
      PdfWriter writer = new PdfWriter(doc);

      int anio = bitacora.has("anio") ? bitacora.get("anio").getAsInt() : 0;
      int mes = bitacora.has("mes") ? bitacora.get("mes").getAsInt() : 0;
      String estado = bitacora.has("estado") ? bitacora.get("estado").getAsString() : "";
      String bitacoraId = bitacora.has("bitacoraId") ? bitacora.get("bitacoraId").getAsString() : "";
      String creadoEn = bitacora.has("creadoEn") ? bitacora.get("creadoEn").getAsString() : "";
      System.out.println("BITACORA JSON PDF = " + bitacora);
      String nombres = "-";
      String apellidos = "-";
      String correoInst = "-";
      if (bitacora.has("estudiante") && bitacora.get("estudiante").isJsonObject()) {
        JsonObject estudiante = bitacora.getAsJsonObject("estudiante");
        if (estudiante.has("nombres") && !estudiante.get("nombres").isJsonNull()) {
          nombres = estudiante.get("nombres").getAsString();
        }
        if (estudiante.has("apellidos") && !estudiante.get("apellidos").isJsonNull()) {
          apellidos = estudiante.get("apellidos").getAsString();
        }
        if (estudiante.has("correoInstitucional") && !estudiante.get("correoInstitucional").isJsonNull()) {
          correoInst = estudiante.get("correoInstitucional").getAsString();
        }
      }

      writer.addLine("BITÁCORA MENSUAL", true);
      writer.addLine("ID: " + bitacoraId, false);
      writer.addLine(String.format("Periodo: %04d-%02d", anio, mes), false);
      writer.addLine("Estado: " + estado, false);
      if (creadoEn != null && !creadoEn.isBlank()) {
        writer.addLine("Creado en: " + creadoEn, false);
      }

      writer.addLine("Nombre del estudiante: " + (nombres + " " + apellidos).trim(), true);
      writer.addLine("Correo institucional: " + correoInst, true);
      writer.addLine("Periodo: " + mes + "/" + anio, true);
      writer.addBlankLine();

      String[] headers = {
          "Semana",
          "Fecha",
          "Hora inicio",
          "Hora salida",
          "Total",
          "Actividades realizadas",
          "Observaciones",
          "Anexos"
      };
        float[] widths = {40f, 70f, 45f, 45f, 35f, 150f, 70f, 60f};
        widths = writer.fitWidthsToPage(widths);
        writer.drawTableHeader(headers, widths);
      writer.addTableGap(4f);

      for (int i = 0; i < semanas.size(); i++) {
        JsonObject s = semanas.get(i).getAsJsonObject();
        String fi = s.has("fechaInicioSemana") ? s.get("fechaInicioSemana").getAsString() : "";
        String ff = s.has("fechaFinSemana") ? s.get("fechaFinSemana").getAsString() : "";
        String actReal = s.has("actividadesRealizadas") ? s.get("actividadesRealizadas").getAsString() : "";
        String obs = s.has("observaciones") && !s.get("observaciones").isJsonNull() ? s.get("observaciones").getAsString() : "";
        String anex = s.has("anexos") && !s.get("anexos").isJsonNull() ? s.get("anexos").getAsString() : "";

        String semanaLabel = "Semana " + (i + 1);
        String fechaLabel = fi + " - " + ff;

        writer.drawTableRow(new String[] {
            semanaLabel,
            fechaLabel,
            "",
            "",
            "",
            actReal,
            obs,
            anex
        }, widths, false);

        if (s.has("actividades") && s.get("actividades").isJsonArray()) {
          JsonArray acts = s.getAsJsonArray("actividades");
          for (int j = 0; j < acts.size(); j++) {
            JsonObject a = acts.get(j).getAsJsonObject();
            String hi = a.has("horaInicio") ? a.get("horaInicio").getAsString() : "";
            String hs = a.has("horaSalida") ? a.get("horaSalida").getAsString() : "";
            String desc = a.has("descripcion") ? a.get("descripcion").getAsString() : "";
            String total = a.has("totalHoras") ? String.format("%.2f", a.get("totalHoras").getAsDouble()) : "";

            writer.drawTableRow(new String[] {
                "",
                "",
                hi,
                hs,
                total,
                desc,
                "",
                ""
            }, widths, false);
          }
        }
      }

      writer.close();
      doc.save(file);
    }
  }

  private static class PdfWriter {
    private final PDDocument doc;
    private PDPage page;
    private PDPageContentStream content;
    private float y;
    private final float margin = 40f;
    private final float leading = 16f;
    private final float fontSize = 10f;
    private final float boldFontSize = 11f;
    private final float maxWidth;
    private String[] tableHeaders;
    private float[] tableWidths;
    private boolean drawingHeader = false;

    PdfWriter(PDDocument doc) throws IOException {
      this.doc = doc;
      this.page = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
      doc.addPage(page);
      this.content = new PDPageContentStream(doc, page);
      this.y = page.getMediaBox().getHeight() - margin;
      this.maxWidth = page.getMediaBox().getWidth() - 2 * margin;
      this.content.setFont(PDType1Font.HELVETICA, fontSize);
    }

    void addLine(String text, boolean bold) throws IOException {
      if (text == null) text = "";
      ensureSpace();
      content.beginText();
      content.setFont(bold ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA, bold ? boldFontSize : fontSize);
      content.newLineAtOffset(margin, y);
      content.showText(text);
      content.endText();
      y -= leading;
    }

    void addBlankLine() throws IOException {
      y -= leading / 2f;
      ensureSpace();
    }

    void drawTableHeader(String[] headers, float[] widths) throws IOException {
      this.tableHeaders = headers;
      this.tableWidths = widths;
      drawTableRow(headers, widths, true);
    }

    void drawTableRow(String[] cells, float[] widths, boolean header) throws IOException {
      if (cells == null || widths == null) return;

      float paddingX = 2f;
      float paddingY = 4f;
      int maxLines = 1;
      for (int i = 0; i < widths.length; i++) {
        String text = i < cells.length ? cells[i] : "";
        float w = Math.max(10f, widths[i] - paddingX * 2);
        int lines = wrapText(text, PDType1Font.HELVETICA, fontSize, w).size();
        if (lines > maxLines) maxLines = lines;
      }

      float rowHeight = maxLines * leading + paddingY * 2;
      ensureSpaceForRow(rowHeight, header);

      float x = margin;
      float topY = y;

      content.setLineWidth(0.5f);
      content.moveTo(margin, topY);
      content.lineTo(margin + sum(widths), topY);
      content.stroke();

      content.moveTo(margin, topY - rowHeight);
      content.lineTo(margin + sum(widths), topY - rowHeight);
      content.stroke();

      float lineX = margin;
      for (float w : widths) {
        content.moveTo(lineX, topY);
        content.lineTo(lineX, topY - rowHeight);
        content.stroke();
        lineX += w;
      }
      content.moveTo(lineX, topY);
      content.lineTo(lineX, topY - rowHeight);
      content.stroke();

      for (int i = 0; i < widths.length; i++) {
        String text = i < cells.length ? cells[i] : "";
        float w = Math.max(10f, widths[i] - paddingX * 2);
        java.util.List<String> lines = wrapText(text, PDType1Font.HELVETICA, fontSize, w);
        float textY = topY - paddingY - fontSize;
        for (int l = 0; l < lines.size(); l++) {
          content.beginText();
          content.setFont(header ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA, header ? boldFontSize : fontSize);
          content.newLineAtOffset(x + paddingX, textY - (leading * l));
          content.showText(lines.get(l));
          content.endText();
        }
        x += widths[i];
      }

      y = topY - rowHeight;
    }

    void ensureSpace() throws IOException {
      if (y <= margin + leading) {
        newPage();
      }
    }

    void ensureSpaceForRow(float rowHeight, boolean header) throws IOException {
      if (y - rowHeight <= margin) {
        newPage();
        if (!header && tableHeaders != null && tableWidths != null && !drawingHeader) {
          drawingHeader = true;
          drawTableRow(tableHeaders, tableWidths, true);
          drawingHeader = false;
        }
      }
    }

    void addTableGap(float gap) throws IOException {
      if (gap <= 0) return;
      y -= gap;
      ensureSpace();
    }

    void newPage() throws IOException {
      content.close();
      page = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
      doc.addPage(page);
      content = new PDPageContentStream(doc, page);
      content.setFont(PDType1Font.HELVETICA, fontSize);
      y = page.getMediaBox().getHeight() - margin;
    }

    float sum(float[] widths) {
      float s = 0f;
      for (float w : widths) s += w;
      return s;
    }

    float[] fitWidthsToPage(float[] widths) {
      if (widths == null || widths.length == 0) return widths;
      float total = sum(widths);
      if (total <= 0f || maxWidth <= 0f) return widths;
      float scale = maxWidth / total;
      float[] scaled = new float[widths.length];
      for (int i = 0; i < widths.length; i++) {
        scaled[i] = widths[i] * scale;
      }
      return scaled;
    }

    void close() throws IOException {
      if (content != null) content.close();
    }
  }

  private static List<String> wrapText(String text, PDType1Font font, float fontSize, float maxWidth) throws IOException {
    List<String> lines = new ArrayList<>();
    if (text == null) {
      lines.add("");
      return lines;
    }
    String[] words = text.split("\\s+");
    StringBuilder line = new StringBuilder();
    for (String word : words) {
      String test = line.length() == 0 ? word : line + " " + word;
      float w = font.getStringWidth(test) / 1000f * fontSize;
      if (w > maxWidth && line.length() > 0) {
        lines.add(line.toString());
        line = new StringBuilder(word);
      } else {
        if (line.length() > 0) line.append(" ");
        line.append(word);
      }
    }
    if (line.length() > 0) lines.add(line.toString());
    return lines;
  }

  private static class BitacoraOption {
    private final String id;
    private final String creadoEn;
    private final String estado;

    private BitacoraOption(String id, String creadoEn, String estado) {
      this.id = id;
      this.creadoEn = creadoEn == null || creadoEn.isBlank() ? "-" : creadoEn;
      this.estado = estado == null || estado.isBlank() ? "-" : estado;
    }

    private String label() {
      return "Creada: " + creadoEn + " | Estado: " + estado;
    }
  }
}
