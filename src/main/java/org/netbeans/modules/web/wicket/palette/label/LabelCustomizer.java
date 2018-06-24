/*
 * Decompiled with CFR 0_130.
 * 
 * Could not load the following classes:
 *  org.openide.DialogDescriptor
 *  org.openide.DialogDisplayer
 *  org.openide.util.NbBundle
 */
package org.netbeans.modules.web.wicket.palette.label;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.web.wicket.palette.label.Label;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

public class LabelCustomizer
extends JPanel {
    private Dialog dialog = null;
    private DialogDescriptor descriptor = null;
    private boolean dialogOK = false;
    Label label;
    JTextComponent target;
    private JTextField initialTextField;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JTextField placeholderTextField;
    private JTextField wicketIdTextField;

    public LabelCustomizer(Label label, JTextComponent target) {
        this.label = label;
        this.target = target;
        this.initComponents();
    }

    public boolean showDialog() {
        this.dialogOK = false;
        String displayName = "";
        try {
            displayName = NbBundle.getBundle((String)"org.netbeans.modules.web.wicket.palette.label.Bundle").getString("NAME_html-LABEL");
        }
        catch (Exception e) {
            // empty catch block
        }
        this.descriptor = new DialogDescriptor((Object)this, NbBundle.getMessage(LabelCustomizer.class, (String)"LBL_Customizer_InsertPrefix") + " " + displayName, true, 2, DialogDescriptor.OK_OPTION, new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (LabelCustomizer.this.descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
                    LabelCustomizer.this.evaluateInput();
                    LabelCustomizer.this.dialogOK = true;
                }
                LabelCustomizer.this.dialog.dispose();
            }
        });
        this.dialog = DialogDisplayer.getDefault().createDialog(this.descriptor);
        this.dialog.setVisible(true);
        this.repaint();
        return this.dialogOK;
    }

    private void evaluateInput() {
        String wicketId = this.wicketIdTextField.getText();
        String placeholderText = this.placeholderTextField.getText();
        String initialValue = this.initialTextField.getText();
        this.label.setWicketId(wicketId);
        this.label.setPlaceholderText(placeholderText);
        this.label.setInitialValue(initialValue);
    }

    private void initComponents() {
        this.jLabel1 = new JLabel();
        this.wicketIdTextField = new JTextField();
        this.jLabel2 = new JLabel();
        this.placeholderTextField = new JTextField();
        this.initialTextField = new JTextField();
        this.jLabel3 = new JLabel();
        this.jLabel1.setText(NbBundle.getMessage(LabelCustomizer.class, (String)"LabelCustomizer.jLabel1.text"));
        this.wicketIdTextField.setText("label-" + System.currentTimeMillis());
        this.jLabel2.setText(NbBundle.getMessage(LabelCustomizer.class, (String)"LabelCustomizer.jLabel2.text"));
        this.placeholderTextField.setText(NbBundle.getMessage(LabelCustomizer.class, (String)"LabelCustomizer.placeholderTextField.text"));
        this.initialTextField.setText(NbBundle.getMessage(LabelCustomizer.class, (String)"LabelCustomizer.initialTextField.text"));
        this.jLabel3.setText(NbBundle.getMessage(LabelCustomizer.class, (String)"LabelCustomizer.jLabel3.text"));
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(this.jLabel1).addComponent(this.jLabel2).addComponent(this.jLabel3)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.wicketIdTextField, -1, 178, 32767).addComponent(this.placeholderTextField, -1, 178, 32767).addComponent(this.initialTextField, -1, 178, 32767)).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(this.jLabel1).addComponent(this.wicketIdTextField, -2, -1, -2)).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(this.jLabel2).addComponent(this.placeholderTextField, -2, -1, -2)).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(this.initialTextField, -2, -1, -2).addComponent(this.jLabel3)).addContainerGap(-1, 32767)));
    }

}

