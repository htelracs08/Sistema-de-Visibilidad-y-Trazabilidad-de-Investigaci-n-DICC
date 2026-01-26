package ec.epn.dicc.director.ui;

import com.google.gson.*;
import ec.epn.dicc.director.api.ApiClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class DirectorFrame extends JFrame {
  private final ApiClient api;

  // Tab Proyectos - TODAS LAS COLUMNAS
  private final DefaultTableModel proyectosModel = new DefaultTableModel(
      new Object[]{"proyectoId", "codigo", "nombre", "directorCorreo", "activo", 
                   "tipo", "subtipo", "fechaInicio", "fechaFin", "maxAyudantes", "maxArticulos"}, 0
  ) {
    @Override public boolean isCellEditable(int r, int c) { return false; }
  };

  // Tab Ayudantes
  private final DefaultTableModel ayudantesModel = new DefaultTableModel(
      new Object[]{"contratoId", "ayudanteId", "correoInstitucional", "nombres", "apellidos", 
                   "estado", "fechaInicio", "fechaFin", "facultad", "quintil", "tipoAyudante"}, 0
  ) {
    @Override public boolean isCellEditable(int r, int c) { return false; }
  };

  // Tab Bitácoras
  private final DefaultTableModel pendientesModel = new DefaultTableModel(
      new Object[]{"bitacoraId", "contratoId", "anio", "mes", "estado", 
                   "correoInstitucional", "nombres", "apellidos"}, 0
  ) {
    @Override public boolean isCellEditable(int r, int c) { return false; }
  };

  // Selección actual
  private String proyectoIdSeleccionado = null;

  public DirectorFrame(ApiClient api) {
    super("Director - Panel");
    this.api = api;

    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setSize(1200, 700);
    setLocationRelativeTo(null);

    JTabbedPane tabs = new JTabbedPane();
    tabs.addTab("Proyectos", buildTabProyectos());
    tabs.addTab("Ayudantes", buildTabAyudantes());
    tabs.addTab("Bitácoras", buildTabBitacoras());

    setContentPane(tabs);

    // carga inicial
    cargarProyectos();
  }

  // =========================
  // TAB: PROYECTOS
  // =========================
  private JPanel buildTabProyectos() {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(new EmptyBorder(12, 12, 12, 12));

    JTable table = new JTable(proyectosModel);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    
    // Ajustar anchos de columnas
    table.getColumnModel().getColumn(0).setPreferredWidth(80);  // proyectoId
    table.getColumnModel().getColumn(1).setPreferredWidth(100); // codigo
    table.getColumnModel().getColumn(2).setPreferredWidth(200); // nombre
    table.getColumnModel().getColumn(3).setPreferredWidth(180); // directorCorreo
    table.getColumnModel().getColumn(4).setPreferredWidth(60);  // activo
    table.getColumnModel().getColumn(5).setPreferredWidth(120); // tipo
    table.getColumnModel().getColumn(6).setPreferredWidth(120); // subtipo
    table.getColumnModel().getColumn(7).setPreferredWidth(100); // fechaInicio
    table.getColumnModel().getColumn(8).setPreferredWidth(100); // fechaFin
    table.getColumnModel().getColumn(9).setPreferredWidth(100); // maxAyudantes
    table.getColumnModel().getColumn(10).setPreferredWidth(100); // maxArticulos
    
    table.getSelectionModel().addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting()) {
        int row = table.getSelectedRow();
        if (row >= 0) {
          proyectoIdSeleccionado = String.valueOf(proyectosModel.getValueAt(row, 0));
        }
      }
    });

    JButton btnRef = new JButton("Refrescar");
    btnRef.addActionListener(e -> cargarProyectos());

    JButton btnUpdate = new JButton("Actualizar Detalles");
    btnUpdate.addActionListener(e -> actualizarDetallesProyecto());

    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
    top.add(btnRef);
    top.add(btnUpdate);

    p.add(top, BorderLayout.NORTH);
    p.add(new JScrollPane(table), BorderLayout.CENTER);

    JLabel hint = new JLabel("Selecciona un proyecto y luego 'Actualizar Detalles' (tipo/subtipo/max...).");
    hint.setBorder(new EmptyBorder(8, 0, 0, 0));
    p.add(hint, BorderLayout.SOUTH);

    return p;
  }

  private void cargarProyectos() {
    proyectosModel.setRowCount(0);

    JsonObject resp = api.get("/api/v1/director/mis-proyectos");
    int httpStatus = resp.get("_httpStatus").getAsInt();
    
    if (httpStatus != 200) {
      showError("No pude cargar proyectos. HTTP " + httpStatus);
      return;
    }

    JsonElement data = resp.get("data");
    if (data == null || !data.isJsonArray()) {
      showError("Respuesta inesperada al listar proyectos.");
      return;
    }

    JsonArray arr = data.getAsJsonArray();
    for (JsonElement el : arr) {
      JsonObject o = el.getAsJsonObject();
      proyectosModel.addRow(new Object[]{
          s(o, "id"),
          s(o, "codigo"),
          s(o, "nombre"),
          s(o, "directorCorreo"),
          o.has("activo") && o.get("activo").getAsBoolean() ? "SÍ" : "NO",
          s(o, "tipo"),
          s(o, "subtipo"),
          s(o, "fechaInicio"),
          s(o, "fechaFin"),
          s(o, "maxAyudantes"),
          s(o, "maxArticulos")
      });
    }
  }

  private void actualizarDetallesProyecto() {
    if (proyectoIdSeleccionado == null || proyectoIdSeleccionado.isEmpty()) {
      showError("Selecciona un proyecto primero.");
      return;
    }

    JTextField fIni = new JTextField();
    JTextField fFin = new JTextField();
    JTextField tipo = new JTextField();
    JTextField subtipo = new JTextField();
    JTextField maxAyu = new JTextField();
    JTextField maxArt = new JTextField();

    JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
    form.add(new JLabel("fechaInicio (YYYY-MM-DD)")); form.add(fIni);
    form.add(new JLabel("fechaFin (YYYY-MM-DD)")); form.add(fFin);
    form.add(new JLabel("tipo")); form.add(tipo);
    form.add(new JLabel("subtipo (o vacío)")); form.add(subtipo);
    form.add(new JLabel("maxAyudantes")); form.add(maxAyu);
    form.add(new JLabel("maxArticulos")); form.add(maxArt);

    int ok = JOptionPane.showConfirmDialog(this, form, "Actualizar Proyecto", JOptionPane.OK_CANCEL_OPTION);
    if (ok != JOptionPane.OK_OPTION) return;

    // Validaciones
    if (fIni.getText().trim().isEmpty() || fFin.getText().trim().isEmpty()) {
      showError("Las fechas son requeridas");
      return;
    }
    
    if (tipo.getText().trim().isEmpty()) {
      showError("El tipo es requerido");
      return;
    }

    try {
      Integer.parseInt(maxAyu.getText().trim());
      Integer.parseInt(maxArt.getText().trim());
    } catch (NumberFormatException e) {
      showError("maxAyudantes y maxArticulos deben ser números válidos");
      return;
    }

    JsonObject body = new JsonObject();
    body.addProperty("fechaInicio", fIni.getText().trim());
    body.addProperty("fechaFin", fFin.getText().trim());
    body.addProperty("tipo", tipo.getText().trim());
    body.addProperty("subtipo", subtipo.getText().trim());
    body.addProperty("maxAyudantes", Integer.parseInt(maxAyu.getText().trim()));
    body.addProperty("maxArticulos", Integer.parseInt(maxArt.getText().trim()));

    JsonObject resp = api.putJson("/api/v1/director/proyectos/" + proyectoIdSeleccionado, body);
    int code = resp.get("_httpStatus").getAsInt();
    
    if (code != 200) {
      JsonElement dataEl = resp.get("data");
      String msg = "Error al actualizar. HTTP " + code;
      if (dataEl != null && dataEl.isJsonObject()) {
        JsonObject dataObj = dataEl.getAsJsonObject();
        if (dataObj.has("msg")) {
          msg += ": " + dataObj.get("msg").getAsString();
        }
      }
      showError(msg);
      return;
    }

    JOptionPane.showMessageDialog(this, "Proyecto actualizado correctamente.");
    cargarProyectos(); // RECARGAR para ver los cambios
  }

  // =========================
  // TAB: AYUDANTES - CORREGIDO
  // =========================
  private JPanel buildTabAyudantes() {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(new EmptyBorder(12, 12, 12, 12));

    JTable table = new JTable(ayudantesModel);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    
    // Ajustar anchos
    table.getColumnModel().getColumn(0).setPreferredWidth(80);  // contratoId
    table.getColumnModel().getColumn(1).setPreferredWidth(80);  // ayudanteId
    table.getColumnModel().getColumn(2).setPreferredWidth(180); // correo
    table.getColumnModel().getColumn(3).setPreferredWidth(100); // nombres
    table.getColumnModel().getColumn(4).setPreferredWidth(100); // apellidos
    table.getColumnModel().getColumn(5).setPreferredWidth(80);  // estado
    table.getColumnModel().getColumn(6).setPreferredWidth(100); // fechaInicio
    table.getColumnModel().getColumn(7).setPreferredWidth(100); // fechaFin
    table.getColumnModel().getColumn(8).setPreferredWidth(60);  // facultad
    table.getColumnModel().getColumn(9).setPreferredWidth(60);  // quintil
    table.getColumnModel().getColumn(10).setPreferredWidth(150); // tipoAyudante

    JButton btnList = new JButton("Listar del Proyecto Seleccionado");
    btnList.addActionListener(e -> listarAyudantes());

    JButton btnReg = new JButton("Registrar Ayudante");
    btnReg.addActionListener(e -> registrarAyudante());

    JButton btnFin = new JButton("Finalizar Contrato");
    btnFin.addActionListener(e -> finalizarContrato(table));

    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
    top.add(btnList);
    top.add(btnReg);
    top.add(btnFin);

    p.add(top, BorderLayout.NORTH);
    p.add(new JScrollPane(table), BorderLayout.CENTER);

    JLabel hint = new JLabel("Necesitas seleccionar un proyecto en la pestaña Proyectos.");
    hint.setBorder(new EmptyBorder(8, 0, 0, 0));
    p.add(hint, BorderLayout.SOUTH);

    return p;
  }

  private void listarAyudantes() {
    if (proyectoIdSeleccionado == null || proyectoIdSeleccionado.isEmpty()) {
      showError("Selecciona un proyecto primero (pestaña Proyectos).");
      return;
    }

    ayudantesModel.setRowCount(0);

    JsonObject resp = api.get("/api/v1/director/proyectos/" + proyectoIdSeleccionado + "/ayudantes");
    int code = resp.get("_httpStatus").getAsInt();
    
    if (code != 200) {
      showError("No pude listar ayudantes. HTTP " + code);
      return;
    }

    JsonElement data = resp.get("data");
    
    // ✅ FIX: El backend retorna directamente un array, no {ok: true, items: [...]}
    if (data == null) {
      showError("Respuesta vacía del servidor.");
      return;
    }

    // El data puede ser un array directamente
    JsonArray arr;
    if (data.isJsonArray()) {
      arr = data.getAsJsonArray();
    } else if (data.isJsonObject() && data.getAsJsonObject().has("items")) {
      arr = data.getAsJsonObject().getAsJsonArray("items");
    } else {
      showError("Formato de respuesta inesperado al listar ayudantes.");
      System.out.println("DEBUG - Response data: " + data);
      return;
    }

    for (JsonElement el : arr) {
      JsonObject o = el.getAsJsonObject();
      ayudantesModel.addRow(new Object[]{
          s(o, "contratoId"),
          s(o, "ayudanteId"),
          s(o, "correoInstitucional"),
          s(o, "nombres"),
          s(o, "apellidos"),
          s(o, "estado"),
          s(o, "fechaInicio"),
          s(o, "fechaFin"),
          s(o, "facultad"),
          s(o, "quintil"),
          s(o, "tipoAyudante")
      });
    }
    
    JOptionPane.showMessageDialog(this, 
        "Se cargaron " + arr.size() + " contratos/ayudantes.", 
        "Éxito", JOptionPane.INFORMATION_MESSAGE);
  }

  private void registrarAyudante() {
    if (proyectoIdSeleccionado == null || proyectoIdSeleccionado.isEmpty()) {
      showError("Selecciona un proyecto primero (pestaña Proyectos).");
      return;
    }

    JTextField nombres = new JTextField();
    JTextField apellidos = new JTextField();
    JTextField correo = new JTextField();
    JTextField facultad = new JTextField("FIS");
    JTextField quintil = new JTextField("2");
    JTextField tipoAyudante = new JTextField("AYUDANTE_INVESTIGACION");
    JTextField fci = new JTextField("2026-01-01");
    JTextField fcf = new JTextField("2026-03-31");

    JPanel form = new JPanel(new GridLayout(8, 2, 10, 10));
    form.add(new JLabel("Nombres *")); form.add(nombres);
    form.add(new JLabel("Apellidos *")); form.add(apellidos);
    form.add(new JLabel("Correo Institucional *")); form.add(correo);
    form.add(new JLabel("Facultad *")); form.add(facultad);
    form.add(new JLabel("Quintil (1-5) *")); form.add(quintil);
    form.add(new JLabel("Tipo Ayudante *")); form.add(tipoAyudante);
    form.add(new JLabel("Fecha Inicio Contrato *")); form.add(fci);
    form.add(new JLabel("Fecha Fin Contrato *")); form.add(fcf);

    int ok = JOptionPane.showConfirmDialog(this, form, "Registrar Ayudante", JOptionPane.OK_CANCEL_OPTION);
    if (ok != JOptionPane.OK_OPTION) return;

    // Validaciones
    if (nombres.getText().trim().isEmpty() || apellidos.getText().trim().isEmpty() ||
        correo.getText().trim().isEmpty() || facultad.getText().trim().isEmpty() ||
        quintil.getText().trim().isEmpty() || tipoAyudante.getText().trim().isEmpty() ||
        fci.getText().trim().isEmpty() || fcf.getText().trim().isEmpty()) {
      showError("Todos los campos son requeridos");
      return;
    }

    int quintilValue;
    try {
      quintilValue = Integer.parseInt(quintil.getText().trim());
      if (quintilValue < 1 || quintilValue > 5) {
        showError("El quintil debe estar entre 1 y 5");
        return;
      }
    } catch (NumberFormatException e) {
      showError("El quintil debe ser un número válido");
      return;
    }

    JsonObject body = new JsonObject();
    body.addProperty("nombres", nombres.getText().trim());
    body.addProperty("apellidos", apellidos.getText().trim());
    body.addProperty("correoInstitucional", correo.getText().trim().toLowerCase());
    body.addProperty("facultad", facultad.getText().trim());
    body.addProperty("quintil", quintilValue);
    body.addProperty("tipoAyudante", tipoAyudante.getText().trim());
    body.addProperty("fechaInicioContrato", fci.getText().trim());
    body.addProperty("fechaFinContrato", fcf.getText().trim());

    JsonObject resp = api.postJson("/api/v1/director/proyectos/" + proyectoIdSeleccionado + "/ayudantes", body);
    int code = resp.get("_httpStatus").getAsInt();
    
    if (code != 200) {
      JsonElement dataEl = resp.get("data");
      String msg = "Error al registrar. HTTP " + code;
      if (dataEl != null && dataEl.isJsonObject()) {
        JsonObject dataObj = dataEl.getAsJsonObject();
        if (dataObj.has("msg")) {
          msg += ": " + dataObj.get("msg").getAsString();
        }
      }
      showError(msg);
      return;
    }

    JOptionPane.showMessageDialog(this, "Ayudante registrado correctamente.");
    listarAyudantes();
  }

  private void finalizarContrato(JTable table) {
    int row = table.getSelectedRow();
    if (row < 0) {
      showError("Selecciona un contrato en la tabla.");
      return;
    }

    String contratoId = String.valueOf(ayudantesModel.getValueAt(row, 0));

    String[] opts = {"RENUNCIA", "FIN_CONTRATO", "DESPIDO"};
    String motivo = (String) JOptionPane.showInputDialog(
        this, "Selecciona el motivo:", "Finalizar Contrato", 
        JOptionPane.QUESTION_MESSAGE, null, opts, opts[0]
    );
    if (motivo == null) return;

    JsonObject body = new JsonObject();
    body.addProperty("motivo", motivo);

    JsonObject resp = api.postJson("/api/v1/director/contratos/" + contratoId + "/finalizar", body);
    int code = resp.get("_httpStatus").getAsInt();
    
    if (code != 200) {
      showError("Error al finalizar. HTTP " + code);
      return;
    }

    JOptionPane.showMessageDialog(this, "Contrato finalizado correctamente.");
    listarAyudantes();
  }

  // =========================
  // TAB: BITÁCORAS - CORREGIDO
  // =========================
  private JPanel buildTabBitacoras() {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(new EmptyBorder(12, 12, 12, 12));

    JTable table = new JTable(pendientesModel);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    JButton btnPend = new JButton("Pendientes del Proyecto Seleccionado");
    btnPend.addActionListener(e -> listarPendientes());

    JButton btnVer = new JButton("Ver Bitácora");
    btnVer.addActionListener(e -> verBitacora(table));

    JButton btnRev = new JButton("Revisar (Aprobar/Rechazar)");
    btnRev.addActionListener(e -> revisarBitacora(table));

    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
    top.add(btnPend);
    top.add(btnVer);
    top.add(btnRev);

    p.add(top, BorderLayout.NORTH);
    p.add(new JScrollPane(table), BorderLayout.CENTER);

    return p;
  }

  private void listarPendientes() {
    if (proyectoIdSeleccionado == null || proyectoIdSeleccionado.isEmpty()) {
      showError("Selecciona un proyecto primero (pestaña Proyectos).");
      return;
    }

    pendientesModel.setRowCount(0);

    JsonObject resp = api.get("/api/v1/director/proyectos/" + proyectoIdSeleccionado + "/bitacoras/pendientes");
    int code = resp.get("_httpStatus").getAsInt();
    
    if (code != 200) {
      showError("No pude listar pendientes. HTTP " + code);
      return;
    }

    JsonElement data = resp.get("data");
    
    // ✅ FIX: Similar al anterior, el backend puede retornar directamente un array
    if (data == null) {
      showError("Respuesta vacía del servidor.");
      return;
    }

    JsonArray arr;
    if (data.isJsonArray()) {
      arr = data.getAsJsonArray();
    } else if (data.isJsonObject() && data.getAsJsonObject().has("items")) {
      arr = data.getAsJsonObject().getAsJsonArray("items");
    } else {
      showError("Formato de respuesta inesperado al listar bitácoras.");
      System.out.println("DEBUG - Response data: " + data);
      return;
    }

    for (JsonElement el : arr) {
      JsonObject o = el.getAsJsonObject();
      pendientesModel.addRow(new Object[]{
          s(o, "bitacoraId"),
          s(o, "contratoId"),
          s(o, "anio"),
          s(o, "mes"),
          s(o, "estado"),
          s(o, "correoInstitucional"),
          s(o, "nombres"),
          s(o, "apellidos")
      });
    }
    
    if (arr.size() == 0) {
      JOptionPane.showMessageDialog(this, 
          "No hay bitácoras pendientes para este proyecto.", 
          "Info", JOptionPane.INFORMATION_MESSAGE);
    } else {
      JOptionPane.showMessageDialog(this, 
          "Se encontraron " + arr.size() + " bitácoras pendientes.", 
          "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }
  }

  private void verBitacora(JTable table) {
    int row = table.getSelectedRow();
    if (row < 0) {
      showError("Selecciona una bitácora.");
      return;
    }
    String bitacoraId = String.valueOf(pendientesModel.getValueAt(row, 0));

    JsonObject resp = api.get("/api/v1/director/bitacoras/" + bitacoraId);
    int code = resp.get("_httpStatus").getAsInt();
    
    if (code != 200) {
      showError("No pude ver bitácora. HTTP " + code);
      return;
    }

    JsonElement dataEl = resp.get("data");
    if (dataEl == null || !dataEl.isJsonObject()) {
      showError("Respuesta inválida al ver bitácora.");
      return;
    }

    showBitacoraDialog(bitacoraId, dataEl.getAsJsonObject());
  }

  private void showBitacoraDialog(String bitacoraId, JsonObject data) {
    JDialog dialog = new JDialog(this, "Bitácora " + bitacoraId, true);
    dialog.setLayout(new BorderLayout(12, 12));
    dialog.setSize(980, 600);
    dialog.setLocationRelativeTo(this);

    DefaultTableModel model = new DefaultTableModel(new Object[]{
        "Semana", "Actividades Semana", "Observaciones", "Anexos",
        "Actividad", "Inicio", "Salida", "Horas"
    }, 0) {
      @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    JTable table = new JTable(model);
    table.setAutoCreateRowSorter(true);
    table.setRowHeight(24);

    TableColumn col0 = table.getColumnModel().getColumn(0);
    col0.setPreferredWidth(140);
    table.getColumnModel().getColumn(1).setPreferredWidth(200);
    table.getColumnModel().getColumn(2).setPreferredWidth(180);
    table.getColumnModel().getColumn(3).setPreferredWidth(120);
    table.getColumnModel().getColumn(4).setPreferredWidth(220);
    table.getColumnModel().getColumn(5).setPreferredWidth(70);
    table.getColumnModel().getColumn(6).setPreferredWidth(70);
    table.getColumnModel().getColumn(7).setPreferredWidth(60);

    List<String[]> pdfRows = new ArrayList<>();

    JsonArray semanas = data.has("semanas") && data.get("semanas").isJsonArray()
        ? data.getAsJsonArray("semanas")
        : new JsonArray();

    for (JsonElement se : semanas) {
      if (!se.isJsonObject()) continue;
      JsonObject semana = se.getAsJsonObject();

      String semanaLabel = (s(semana, "fechaInicioSemana") + " - " + s(semana, "fechaFinSemana")).trim();
      String actSemana = s(semana, "actividadesRealizadas");
      String obs = s(semana, "observaciones");
      String anexos = s(semana, "anexos");

      JsonArray actividades = semana.has("actividades") && semana.get("actividades").isJsonArray()
          ? semana.getAsJsonArray("actividades")
          : new JsonArray();

      if (actividades.size() == 0) {
        String[] row = new String[]{semanaLabel, actSemana, obs, anexos, "", "", "", ""};
        model.addRow(row);
        pdfRows.add(row);
        continue;
      }

      for (JsonElement ae : actividades) {
        if (!ae.isJsonObject()) continue;
        JsonObject act = ae.getAsJsonObject();

        String desc = s(act, "descripcion");
        String hIni = s(act, "horaInicio");
        String hFin = s(act, "horaSalida");
        String horas = s(act, "totalHoras");

        String[] row = new String[]{semanaLabel, actSemana, obs, anexos, desc, hIni, hFin, horas};
        model.addRow(row);
        pdfRows.add(row);
      }
    }

    JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton btnPdf = new JButton("Descargar PDF");
    JButton btnCerrar = new JButton("Cerrar");

    btnPdf.addActionListener(e -> exportBitacoraPdf(bitacoraId, pdfRows));
    btnCerrar.addActionListener(e -> dialog.dispose());

    footer.add(btnPdf);
    footer.add(btnCerrar);

    dialog.add(new JScrollPane(table), BorderLayout.CENTER);
    dialog.add(footer, BorderLayout.SOUTH);
    dialog.setVisible(true);
  }

  private void exportBitacoraPdf(String bitacoraId, List<String[]> rows) {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Guardar Bitácora como PDF");
    chooser.setSelectedFile(new File("bitacora_" + bitacoraId + ".pdf"));

    int result = chooser.showSaveDialog(this);
    if (result != JFileChooser.APPROVE_OPTION) return;

    File file = chooser.getSelectedFile();

    try {
      generateBitacoraPdf(file, bitacoraId, rows);
      JOptionPane.showMessageDialog(this, "PDF generado correctamente.");
    } catch (IOException ex) {
      showError("No pude generar el PDF: " + ex.getMessage());
    }
  }

  private void generateBitacoraPdf(File file, String bitacoraId, List<String[]> rows) throws IOException {
    try (PDDocument doc = new PDDocument()) {
      PDRectangle base = PDRectangle.LETTER;
      PDRectangle pageSize = new PDRectangle(base.getHeight(), base.getWidth());
      PDPage page = new PDPage(pageSize);
      doc.addPage(page);

      float margin = 36f;
      float yStart = pageSize.getHeight() - margin;
      float y = yStart;
      float fontSize = 8f;
      float leading = 10f;

      List<String> lines = new ArrayList<>();
      lines.add("Bitácora " + bitacoraId);
      lines.add(" ");

      String[] header = new String[]{"Semana", "Act.Semana", "Obs", "Anexos", "Actividad", "Ini", "Fin", "Hrs"};
      int[] widths = new int[]{12, 14, 12, 8, 28, 5, 5, 4};

      lines.add(formatLine(header, widths));
      lines.add(repeat("-", sum(widths) + (widths.length - 1) * 3));

      for (String[] row : rows) {
        lines.addAll(wrapRow(row, widths));
      }

      PDPageContentStream cs = new PDPageContentStream(doc, page);
      cs.setFont(PDType1Font.COURIER, fontSize);
      cs.beginText();
      cs.newLineAtOffset(margin, y);

      for (String line : lines) {
        if (y <= margin) {
          cs.endText();
          cs.close();

          page = new PDPage(pageSize);
          doc.addPage(page);
          cs = new PDPageContentStream(doc, page);
          cs.setFont(PDType1Font.COURIER, fontSize);
          y = yStart;
          cs.beginText();
          cs.newLineAtOffset(margin, y);
        }

        cs.showText(line);
        cs.newLineAtOffset(0, -leading);
        y -= leading;
      }

      cs.endText();
      cs.close();

      doc.save(file);
    }
  }

  private List<String> wrapRow(String[] row, int[] widths) {
    List<List<String>> wrapped = new ArrayList<>();
    int maxLines = 1;
    for (int i = 0; i < widths.length; i++) {
      String text = i < row.length ? safe(row[i]) : "";
      List<String> parts = wrapCell(text, widths[i]);
      wrapped.add(parts);
      if (parts.size() > maxLines) maxLines = parts.size();
    }

    List<String> lines = new ArrayList<>();
    for (int line = 0; line < maxLines; line++) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < widths.length; i++) {
        String part = line < wrapped.get(i).size() ? wrapped.get(i).get(line) : "";
        sb.append(padRight(part, widths[i]));
        if (i < widths.length - 1) sb.append(" | ");
      }
      lines.add(sb.toString());
    }
    return lines;
  }

  private List<String> wrapCell(String text, int width) {
    List<String> out = new ArrayList<>();
    String t = safe(text);
    if (t.isEmpty()) {
      out.add("");
      return out;
    }

    String[] words = t.split("\\s+");
    StringBuilder line = new StringBuilder();
    for (String word : words) {
      if (word.length() > width) {
        if (line.length() > 0) {
          out.add(line.toString());
          line.setLength(0);
        }
        int i = 0;
        while (i < word.length()) {
          int end = Math.min(i + width, word.length());
          out.add(word.substring(i, end));
          i = end;
        }
        continue;
      }

      if (line.length() == 0) {
        line.append(word);
      } else if (line.length() + 1 + word.length() <= width) {
        line.append(' ').append(word);
      } else {
        out.add(line.toString());
        line.setLength(0);
        line.append(word);
      }
    }

    if (line.length() > 0) out.add(line.toString());
    return out;
  }

  private String formatLine(String[] cells, int[] widths) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < widths.length; i++) {
      String cell = i < cells.length ? safe(cells[i]) : "";
      sb.append(padRight(cell, widths[i]));
      if (i < widths.length - 1) sb.append(" | ");
    }
    return sb.toString();
  }

  private String padRight(String text, int width) {
    String t = safe(text);
    if (t.length() >= width) return t.substring(0, width);
    return t + " ".repeat(width - t.length());
  }

  private String repeat(String s, int count) {
    return s.repeat(Math.max(0, count));
  }

  private int sum(int[] arr) {
    int total = 0;
    for (int v : arr) total += v;
    return total;
  }

  private String safe(String v) {
    return v == null ? "" : v.replace("\n", " ").replace("\r", " ").trim();
  }

  private void revisarBitacora(JTable table) {
    int row = table.getSelectedRow();
    if (row < 0) {
      showError("Selecciona una bitácora.");
      return;
    }

    String bitacoraId = String.valueOf(pendientesModel.getValueAt(row, 0));

    String[] opts = {"APROBAR", "RECHAZAR"};
    String decision = (String) JOptionPane.showInputDialog(
        this, "Decisión:", "Revisar Bitácora", 
        JOptionPane.QUESTION_MESSAGE, null, opts, opts[0]
    );
    if (decision == null) return;

    String obs = JOptionPane.showInputDialog(this, "Observación (opcional):", "");
    if (obs == null) obs = "";

    JsonObject body = new JsonObject();
    body.addProperty("decision", decision);
    body.addProperty("observacion", obs);

    JsonObject resp = api.postJson("/api/v1/director/bitacoras/" + bitacoraId + "/revisar", body);
    int code = resp.get("_httpStatus").getAsInt();
    
    if (code != 200) {
      JsonElement dataEl = resp.get("data");
      String msg = "No pude revisar. HTTP " + code;
      if (dataEl != null && dataEl.isJsonObject()) {
        JsonObject dataObj = dataEl.getAsJsonObject();
        if (dataObj.has("msg")) {
          msg += ": " + dataObj.get("msg").getAsString();
        }
      }
      showError(msg);
      return;
    }

    JOptionPane.showMessageDialog(this, "Revisión realizada correctamente.");
    listarPendientes();
  }

  // =========================
  // Helpers UI
  // =========================
  private void showError(String msg) {
    JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
  }

  private String s(JsonObject o, String key) {
    if (o == null || !o.has(key) || o.get(key).isJsonNull()) return "";
    JsonElement el = o.get(key);
    if (el.isJsonPrimitive()) {
      return el.getAsString();
    }
    return el.toString();
  }
}