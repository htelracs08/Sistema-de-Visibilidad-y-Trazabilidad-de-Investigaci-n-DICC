package ec.epn.dicc.ayudante.ui.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Supplier;

public class NuevaActividadDialog extends JDialog {
  private boolean ok = false;

  private Supplier<String> onOkValidation;

  private final JTextField txtHI = new JTextField();
  private final JTextField txtHS = new JTextField();
  private final JTextArea txtDesc = new JTextArea(4, 30);
  private final boolean clearOnShow;

  public NuevaActividadDialog(Frame owner) {
    this(owner, "Nueva actividad", "Crear", "", "",
      "");
  }

  public NuevaActividadDialog(Frame owner, String title, String okText,
                              String horaInicio, String horaSalida, String descripcion) {
    super(owner, title, true);
    setSize(520, 320);
    setLocationRelativeTo(owner);

    JPanel root = new JPanel(new BorderLayout(10, 10));
    root.setBorder(new EmptyBorder(12, 12, 12, 12));

    JPanel form = new JPanel(new GridLayout(2, 2, 10, 10));
    form.add(new JLabel("Hora inicio (HH:mm):"));
    form.add(txtHI);
    form.add(new JLabel("Hora salida (HH:mm):"));
    form.add(txtHS);

    if (horaInicio != null) txtHI.setText(horaInicio);
    if (horaSalida != null) txtHS.setText(horaSalida);
    if (descripcion != null) txtDesc.setText(descripcion);

    boolean hasPrefill = (horaInicio != null && !horaInicio.isBlank())
      || (horaSalida != null && !horaSalida.isBlank())
      || (descripcion != null && !descripcion.isBlank());
    clearOnShow = !hasPrefill;

    JPanel desc = new JPanel(new BorderLayout(6, 6));
    desc.add(new JLabel("Descripción:"), BorderLayout.NORTH);
    desc.add(new JScrollPane(txtDesc), BorderLayout.CENTER);

    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton btnCancel = new JButton("Cancelar");
    JButton btnOk = new JButton(okText == null ? "Aceptar" : okText);

    btnCancel.addActionListener(e -> dispose());
    btnOk.addActionListener(e -> {
      if (onOkValidation != null) {
        String err = onOkValidation.get();
        if (err != null && !err.isBlank()) {
          JOptionPane.showMessageDialog(this, err, "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }
      }
      ok = true;
      dispose();
    });

    actions.add(btnCancel);
    actions.add(btnOk);

    root.add(form, BorderLayout.NORTH);
    root.add(desc, BorderLayout.CENTER);
    root.add(actions, BorderLayout.SOUTH);

    setContentPane(root);
  }

  private void clearForm() {
    txtHI.setText("");
    txtHS.setText("");
    txtDesc.setText("");
  }

  @Override
  public void setVisible(boolean b) {
    if (b && clearOnShow) clearForm();
    super.setVisible(b);
  }

  public void setOnOkValidation(Supplier<String> onOkValidation) {
    this.onOkValidation = onOkValidation;
  }

  public boolean isOk() { return ok; }

  public String getHoraInicio() { return txtHI.getText().trim(); }
  public String getHoraSalida() { return txtHS.getText().trim(); }
  public String getDescripcion() { return txtDesc.getText().trim(); }
}
