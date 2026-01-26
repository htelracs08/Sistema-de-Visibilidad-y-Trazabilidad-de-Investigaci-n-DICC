package ec.epn.dicc.ayudante.ui.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class NuevaSemanaDialog extends JDialog {
  private boolean ok = false;

  private final JTextField txtFI = new JTextField();
  private final JTextField txtFF = new JTextField();
  private final JTextArea txtAct = new JTextArea(4, 30);
  private final JTextArea txtObs = new JTextArea(3, 30);
  private final JTextField txtAnex = new JTextField();

  public NuevaSemanaDialog(Frame owner) {
    super(owner, "Nueva Semana", true);
    setSize(560, 420);
    setLocationRelativeTo(owner);

    JPanel root = new JPanel(new BorderLayout(10, 10));
    root.setBorder(new EmptyBorder(12, 12, 12, 12));

    JPanel form = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(6, 6, 6, 6);
    c.fill = GridBagConstraints.HORIZONTAL;

    int y = 0;

    c.gridx=0; c.gridy=y; c.weightx=0; form.add(new JLabel("Fecha inicio (YYYY-MM-DD):"), c);
    c.gridx=1; c.gridy=y++; c.weightx=1; form.add(txtFI, c);

    c.gridx=0; c.gridy=y; c.weightx=0; form.add(new JLabel("Fecha fin (YYYY-MM-DD):"), c);
    c.gridx=1; c.gridy=y++; c.weightx=1; form.add(txtFF, c);

    c.gridx=0; c.gridy=y; c.weightx=0; c.anchor = GridBagConstraints.NORTHWEST;
    form.add(new JLabel("Actividades realizadas:"), c);
    c.gridx=1; c.gridy=y++; c.weightx=1; c.fill = GridBagConstraints.BOTH;
    form.add(new JScrollPane(txtAct), c);

    c.gridx=0; c.gridy=y; c.weightx=0; c.fill = GridBagConstraints.HORIZONTAL; c.anchor = GridBagConstraints.NORTHWEST;
    form.add(new JLabel("Observaciones:"), c);
    c.gridx=1; c.gridy=y++; c.weightx=1; c.fill = GridBagConstraints.BOTH;
    form.add(new JScrollPane(txtObs), c);

    c.gridx=0; c.gridy=y; c.weightx=0; c.fill = GridBagConstraints.HORIZONTAL; c.anchor = GridBagConstraints.WEST;
    form.add(new JLabel("Anexos:"), c);
    c.gridx=1; c.gridy=y++; c.weightx=1; form.add(txtAnex, c);

    clearForm();

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

    root.add(form, BorderLayout.CENTER);
    root.add(actions, BorderLayout.SOUTH);

    setContentPane(root);
  }

  private void clearForm() {
    txtFI.setText("");
    txtFF.setText("");
    txtAct.setText("");
    txtObs.setText("");
    txtAnex.setText("");
  }

  @Override
  public void setVisible(boolean b) {
    if (b) clearForm();
    super.setVisible(b);
  }

  public boolean isOk() { return ok; }

  public String getFechaInicio() { return txtFI.getText().trim(); }
  public String getFechaFin() { return txtFF.getText().trim(); }
  public String getActividadesRealizadas() { return txtAct.getText().trim(); }
  public String getObservaciones() { return txtObs.getText().trim(); }
  public String getAnexos() { return txtAnex.getText().trim(); }
}
