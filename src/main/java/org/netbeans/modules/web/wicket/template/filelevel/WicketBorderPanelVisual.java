/*
 * Decompiled with CFR 0_130.
 * 
 * Could not load the following classes:
 *  org.netbeans.api.project.Project
 *  org.openide.WizardDescriptor
 *  org.openide.util.HelpCtx
 *  org.openide.util.HelpCtx$Provider
 *  org.openide.util.NbBundle
 */
package org.netbeans.modules.web.wicket.template.filelevel;

import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;
import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.wicket.template.filelevel.WicketPagePanelVisual;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class WicketBorderPanelVisual extends JPanel implements HelpCtx.Provider, ListDataListener {

    public JComboBox jComboBoxSuperclass;
    private JLabel jLabel1;
    private JLabel jLabelSuperclass;

    public WicketBorderPanelVisual(Project proj) {
        this.initComponents();
        this.jComboBoxSuperclass.getModel().addListDataListener(this);
    }

    private void initComponents() {
        this.jLabelSuperclass = new JLabel();
        this.jComboBoxSuperclass = new JComboBox();
        this.jLabel1 = new JLabel();
        this.jLabelSuperclass.setDisplayedMnemonic(NbBundle.getMessage(WicketBorderPanelVisual.class, (String)"LBL_Superlass_mnem").charAt(0));
        this.jLabelSuperclass.setLabelFor(this.jComboBoxSuperclass);
        this.jLabelSuperclass.setText("Superclass:");
        this.jComboBoxSuperclass.setEditable(true);
        this.jComboBoxSuperclass.setModel(new DefaultComboBoxModel<String>(new String[]{"org.apache.wicket.markup.html.border.Border"}));
        this.jLabel1.setText("An HTML file and a Java source file will be created. The extension class is below.");
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.jLabel1).addGroup(layout.createSequentialGroup().addComponent(this.jLabelSuperclass).addGap(12, 12, 12).addComponent(this.jComboBoxSuperclass, -2, 299, -2)));
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(this.jLabel1).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(4, 4, 4).addComponent(this.jLabelSuperclass)).addComponent(this.jComboBoxSuperclass, -2, -1, -2)).addContainerGap(-1, 32767)));
    }

    boolean valid(WizardDescriptor wizardDescriptor) {
        String superclass = (String)this.jComboBoxSuperclass.getEditor().getItem();
        if (superclass == null || superclass.trim().equals("")) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", (Object)NbBundle.getMessage(WicketPagePanelVisual.class, (String)"MSG_NoSuperClassSelected"));
        }
        return true;
    }

    void read(WizardDescriptor settings) {
    }

    void store(WizardDescriptor settings) {
        settings.putProperty("wicketPageSuperclass", this.jComboBoxSuperclass.getSelectedItem());
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(WicketPagePanelVisual.class);
    }

    @Override
    public void intervalRemoved(ListDataEvent e) {
    }

    @Override
    public void intervalAdded(ListDataEvent e) {
    }

    @Override
    public void contentsChanged(ListDataEvent e) {
    }

    public String setjComboBoxSuperclass(String resource) {
        String superclass = (String)this.jComboBoxSuperclass.getEditor().getItem();
        return superclass;
    }
}
