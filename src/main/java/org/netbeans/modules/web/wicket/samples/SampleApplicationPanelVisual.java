/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.samples;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import javax.swing.AbstractButton;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tim Boudreau
 */
public class SampleApplicationPanelVisual extends JPanel implements DocumentListener, FocusListener {

    public static final String PROP_PROJECT_NAME = "projectName";

    private SampleApplicationWizardPanel panel;
    private JButton browseButton;
    private JLabel createdFolderLabel;
    private JTextField createdFolderTextField;
    private JLabel projectLocationLabel;
    private JTextField projectLocationTextField;
    private JLabel projectNameLabel;
    private JTextField projectNameTextField;

    public SampleApplicationPanelVisual(SampleApplicationWizardPanel panel) {
        this.initComponents();
        this.panel = panel;
        this.projectNameTextField.getDocument().addDocumentListener(this);
        this.projectLocationTextField.getDocument().addDocumentListener(this);
        for (Component c : this.getComponents()) {
            if (!(c instanceof JTextField)) {
                continue;
            }
            c.addFocusListener(this);
        }
    }

    public String getProjectName() {
        return this.projectNameTextField.getText();
    }

    private void initComponents() {
        this.projectNameLabel = new JLabel();
        this.projectNameTextField = new JTextField();
        this.projectLocationLabel = new JLabel();
        this.projectLocationTextField = new JTextField();
        this.browseButton = new JButton();
        this.createdFolderLabel = new JLabel();
        this.createdFolderTextField = new JTextField();
        this.projectNameLabel.setLabelFor(this.projectNameTextField);
        Mnemonics.setLocalizedText((JLabel)this.projectNameLabel, (String)"Project &Name:");
        this.projectLocationLabel.setLabelFor(this.projectLocationTextField);
        Mnemonics.setLocalizedText((JLabel)this.projectLocationLabel, (String)"Project &Location:");
        Mnemonics.setLocalizedText((AbstractButton)this.browseButton, (String)"Br&owse...");
        this.browseButton.setActionCommand("BROWSE");
        this.browseButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                SampleApplicationPanelVisual.this.browseButtonActionPerformed(evt);
            }
        });
        this.createdFolderLabel.setLabelFor(this.createdFolderTextField);
        Mnemonics.setLocalizedText((JLabel)this.createdFolderLabel, (String)"Project &Folder:");
        this.createdFolderTextField.setEditable(false);
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.projectNameLabel).addComponent(this.projectLocationLabel).addComponent(this.createdFolderLabel)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.projectNameTextField, GroupLayout.Alignment.TRAILING, -1, 191, 32767).addComponent(this.projectLocationTextField, GroupLayout.Alignment.TRAILING, -1, 191, 32767).addComponent(this.createdFolderTextField, GroupLayout.Alignment.TRAILING, -1, 191, 32767)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.browseButton).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(this.projectNameLabel).addComponent(this.projectNameTextField, -2, -1, -2)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(this.projectLocationLabel).addComponent(this.projectLocationTextField, -2, -1, -2).addComponent(this.browseButton)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(this.createdFolderLabel).addComponent(this.createdFolderTextField, -2, -1, -2)).addContainerGap(213, 32767)));
    }

    private void browseButtonActionPerformed(ActionEvent evt) {
        String command = evt.getActionCommand();
        if ("BROWSE".equals(command)) {
            File f;
            JFileChooser chooser = new JFileChooser();
            FileUtil.preventFileChooserSymlinkTraversal((JFileChooser)chooser, null);
            chooser.setDialogTitle("Select Project Location");
            chooser.setFileSelectionMode(1);
            String path = this.projectLocationTextField.getText();
            if (path.length() > 0 && (f = new File(path)).exists()) {
                chooser.setSelectedFile(f);
            }
            if (0 == chooser.showOpenDialog(this)) {
                File projectDir = chooser.getSelectedFile();
                this.projectLocationTextField.setText(FileUtil.normalizeFile((File)projectDir).getAbsolutePath());
            }
            this.panel.fireChangeEvent();
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        this.projectNameTextField.requestFocus();
    }

    boolean valid(WizardDescriptor wizardDescriptor) {
        File destFolder;
        File projLoc;
        if (this.projectNameTextField.getText().length() == 0) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", (Object)"Project Name is not a valid folder name.");
            return false;
        }
        File f = FileUtil.normalizeFile((File)new File(this.projectLocationTextField.getText()).getAbsoluteFile());
        if (!f.isDirectory()) {
            String message = "Project Folder is not a valid path.";
            wizardDescriptor.putProperty("WizardPanel_errorMessage", (Object)message);
            return false;
        }
        for (projLoc = destFolder = FileUtil.normalizeFile((File)new File((String)this.createdFolderTextField.getText()).getAbsoluteFile()); projLoc != null && !projLoc.exists(); projLoc = projLoc.getParentFile()) {
        }
        if (projLoc == null || !projLoc.canWrite()) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", (Object)"Project Folder cannot be created.");
            return false;
        }
        if (FileUtil.toFileObject((File)projLoc) == null) {
            String message = "Project Folder is not a valid path.";
            wizardDescriptor.putProperty("WizardPanel_errorMessage", (Object)message);
            return false;
        }
        File[] kids = destFolder.listFiles();
        if (destFolder.exists() && kids != null && kids.length > 0) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", (Object)"Project Folder already exists and is not empty.");
            return false;
        }
        wizardDescriptor.putProperty("WizardPanel_errorMessage", (Object)"");
        return true;
    }

    void store(WizardDescriptor d) {
        String name = this.projectNameTextField.getText().trim();
        String folder = this.createdFolderTextField.getText().trim();
        d.putProperty("projdir", (Object)new File(folder));
        d.putProperty("name", (Object)name);
    }

    void read(WizardDescriptor settings) {
        File projectLocation = (File)settings.getProperty("projdir");
        projectLocation = projectLocation == null || projectLocation.getParentFile() == null || !projectLocation.getParentFile().isDirectory() ? ProjectChooser.getProjectsFolder() : projectLocation.getParentFile();
        this.projectLocationTextField.setText(projectLocation.getAbsolutePath());
        String projectName = (String)settings.getProperty("name");
        this.projectNameTextField.setText(projectName);
        this.projectNameTextField.selectAll();
    }

    void validate(WizardDescriptor d) throws WizardValidationException {
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        this.updateTexts(e);
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            this.firePropertyChange(PROP_PROJECT_NAME, null, this.projectNameTextField.getText());
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        this.updateTexts(e);
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            this.firePropertyChange(PROP_PROJECT_NAME, null, this.projectNameTextField.getText());
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        this.updateTexts(e);
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            this.firePropertyChange(PROP_PROJECT_NAME, null, this.projectNameTextField.getText());
        }
    }

    private void updateTexts(DocumentEvent e) {
        Document doc = e.getDocument();
        if (doc == this.projectNameTextField.getDocument() || doc == this.projectLocationTextField.getDocument()) {
            String projectName = this.projectNameTextField.getText();
            String projectFolder = this.projectLocationTextField.getText();
            this.createdFolderTextField.setText(projectFolder + File.separatorChar + projectName);
        }
        this.panel.fireChangeEvent();
    }

    @Override
    public void focusGained(FocusEvent e) {
        ((JTextField)e.getComponent()).selectAll();
    }

    @Override
    public void focusLost(FocusEvent e) {
    }

}
