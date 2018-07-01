/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.template.filelevel;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.api.project.Project;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Tim Boudreau
 */
public class WicketBorderPanelVisual extends JPanel implements HelpCtx.Provider, ListDataListener {

    private JComboBox jComboBoxSuperclass;
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
        this.jComboBoxSuperclass.setModel(new DefaultComboBoxModel<>(new String[]{"org.apache.wicket.markup.html.border.Border"}));
        this.jLabel1.setText("An HTML file and a Java source file will be created. The extension class is below.");
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.jLabel1).addGroup(layout.createSequentialGroup().addComponent(this.jLabelSuperclass).addGap(12, 12, 12).addComponent(this.jComboBoxSuperclass, -2, 299, -2)));
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(this.jLabel1).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(4, 4, 4).addComponent(this.jLabelSuperclass)).addComponent(this.jComboBoxSuperclass, -2, -1, -2)).addContainerGap(-1, 32767)));
    }

    boolean valid(WizardDescriptor wizardDescriptor) {
        String superclass = (String)this.jComboBoxSuperclass.getEditor().getItem();
        if (superclass == null || superclass.trim().equals("")) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(WicketPagePanelVisual.class, "MSG_NoSuperClassSelected"));
        }
        return true;
    }

    void read(WizardDescriptor settings) {
    }

    void store(WizardDescriptor settings) {
        settings.putProperty("wicketPageSuperclass", this.jComboBoxSuperclass.getSelectedItem());
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.web.wicket.template.filelevel.WicketPagePanelVisual");
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

    public String getSelectedSuperclass() {
        String superclass = (String)this.jComboBoxSuperclass.getEditor().getItem();
        return superclass;
    }
}
