package ec.epn.dicc.ayudante.ui.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class NuevaActividadDialog extends JDialog {
  private boolean ok = false;

  private final JTextField txtHI = new JTextField("08:00");
  private final JTextField txtHS = new JTextField("10:00");
  private final JTextArea txtDesc = new JTextArea(4, 30);

  public NuevaActividadDialog(Frame owner) {
    super(owner, "Nueva Actividad", true);
    setSize(520, 320);
    setLocationRelativeTo(owner);

    JPanel root = new JPanel(new BorderLayout(10, 10));
    root.setBorder(new EmptyBorder(12, 12, 12, 12));

    JPanel form = new JPanel(new GridLayout(2, 2, 10, 10));
    form.add(new JLabel("Hora inicio (HH:mm):"));
    form.add(txtHI);
    form.add(new JLabel("Hora salida (HH:mm):"));
    form.add(txtHS);

    txtDesc.setText("Reunión con director para revisión de avances");

    JPanel desc = new JPanel(new BorderLayout(6, 6));
    desc.add(new JLabel("Descripción:"), BorderLayout.NORTH);
    desc.add(new JScrollPane(txtDesc), BorderLayout.CENTER);

    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton btnCancel = new JButton("Cancelar");
    JButton btnOk = new JButton("Crear");

    btnCancel.addActionListener(e -> dispose());
    btnOk.addActionListener(e -> {
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

  public boolean isOk() { return ok; }

  public String getHoraInicio() { return txtHI.getText().trim(); }
  public String getHoraSalida() { return txtHS.getText().trim(); }
  public String getDescripcion() { return txtDesc.getText().trim(); }
}
