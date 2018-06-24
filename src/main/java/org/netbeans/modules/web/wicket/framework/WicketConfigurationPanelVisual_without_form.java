/*
 * Decompiled with CFR 0_130.
 * 
 * Could not load the following classes:
 *  org.netbeans.api.project.libraries.Library
 *  org.netbeans.api.project.libraries.LibraryManager
 *  org.openide.WizardDescriptor
 *  org.openide.WizardValidationException
 *  org.openide.filesystems.FileObject
 *  org.openide.filesystems.FileUtil
 *  org.openide.util.Exceptions
 *  org.openide.util.HelpCtx
 *  org.openide.util.HelpCtx$Provider
 *  org.openide.util.NbBundle
 *  org.openide.xml.XMLUtil
 */
package org.netbeans.modules.web.wicket.framework;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.ResourceBundle;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class WicketConfigurationPanelVisual_without_form extends JPanel implements HelpCtx.Provider, DocumentListener {

    private WicketWebModuleExtender panel;
    private JComboBox jComboBoxURLPattern;
    private JLabel jLabel1;
    private JLabel jLabelAppResource;
    private JLabel jLabelPkgResource;
    private JLabel jLabelServletName;
    private JLabel jLabelURLPattern;
    private JLabel jLabelWicketHomePage;
    private JScrollPane jScrollPane1;
    private JTextArea jTextArea1;
    private JTextArea jTextArea2;
    private JTextField jTextFieldAppResource;
    private JTextField jTextFieldPkgResource;
    private JTextField jTextFieldServletName;
    private JTextField jTextFieldWebPageResource;
    private JButton jbBrowse;
    private JPanel jpLibrary;
    private JPanel jpSetup;
    private JTextField jtFolder;
    private JList versionList;

    public WicketConfigurationPanelVisual_without_form(WicketWebModuleExtender panel, WicketFrameworkProvider framework, boolean enableComponents) {
        this.panel = panel;
        this.initComponents();
        this.refreshPanels();
        this.jTextFieldAppResource.getDocument().addDocumentListener(this);
        ((JTextComponent)this.jComboBoxURLPattern.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);
        this.enableComponents(enableComponents);
        DefaultListModel<String> versionModel = new DefaultListModel<String>();
        FileObject versionFo = FileUtil.getConfigFile((String)"org-netbeans-api-project-libraries/Libraries");
        if (versionFo != null) {
            FileObject[] versionKids = versionFo.getChildren();
            for (FileObject versionKid : FileUtil.getOrder(Arrays.asList(versionKids), (boolean)true)) {
                if (!versionKid.getName().startsWith("Wicket")) {
                    continue;
                }
                try {
                    String libraryName = this.parseFile(versionKid.getInputStream());
                    versionModel.add(0, libraryName);
                } catch (FileNotFoundException ex) {
                    Exceptions.printStackTrace((Throwable)ex);
                }
            }
            this.versionList.setModel(versionModel);
            this.versionList.setSelectedIndex(0);
        }
    }

    private void initComponents() {
        this.jpSetup = new JPanel();
        this.jLabelServletName = new JLabel();
        this.jTextFieldServletName = new JTextField();
        this.jComboBoxURLPattern = new JComboBox();
        this.jLabelURLPattern = new JLabel();
        this.jLabelAppResource = new JLabel();
        this.jTextFieldAppResource = new JTextField();
        this.jLabelWicketHomePage = new JLabel();
        this.jTextFieldWebPageResource = new JTextField();
        this.jLabelPkgResource = new JLabel();
        this.jTextFieldPkgResource = new JTextField();
        this.jLabel1 = new JLabel();
        this.jScrollPane1 = new JScrollPane();
        this.versionList = new JList();
        this.jpLibrary = new JPanel();
        this.jtFolder = new JTextField();
        this.jbBrowse = new JButton();
        this.jTextArea1 = new JTextArea();
        this.jTextArea2 = new JTextArea();
        this.setLayout(new CardLayout());
        this.jLabelServletName.setLabelFor(this.jComboBoxURLPattern);
        this.jLabelServletName.setText("Wicket Filter Name:");
        this.jTextFieldServletName.setEditable(false);
        this.jTextFieldServletName.setText("WicketApplication");
        this.jComboBoxURLPattern.setEditable(true);
        this.jComboBoxURLPattern.setModel(new DefaultComboBoxModel<String>(new String[]{"/wicket/*"}));
        this.jLabelURLPattern.setLabelFor(this.jComboBoxURLPattern);
        this.jLabelURLPattern.setText(NbBundle.getMessage(WicketConfigurationPanelVisual_without_form.class, "LBL_ConfigPanel_URLPattern"));
        this.jLabelAppResource.setLabelFor(this.jTextFieldAppResource);
        this.jLabelAppResource.setText(NbBundle.getMessage(WicketConfigurationPanelVisual_without_form.class, "LBL_ConfigPanel_ApplicationResource"));
        this.jLabelWicketHomePage.setText("Wicket Home Page:");
        this.jLabelPkgResource.setLabelFor(this.jComboBoxURLPattern);
        this.jLabelPkgResource.setText("Main Package:");
        this.jLabel1.setText("Version:");
        this.versionList.setDropMode(DropMode.ON);
        this.jScrollPane1.setViewportView(this.versionList);
        GroupLayout jpSetupLayout = new GroupLayout(this.jpSetup);
        this.jpSetup.setLayout(jpSetupLayout);
        jpSetupLayout.setHorizontalGroup(
                jpSetupLayout
                        .createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(
                                jpSetupLayout
                                        .createSequentialGroup()
                                        .addGroup(
                                                jpSetupLayout
                                                        .createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(this.jLabelAppResource)
                                                        .addComponent(this.jLabelServletName)
                                                        .addComponent(this.jLabelURLPattern)
                                                        .addComponent(this.jLabelWicketHomePage)
                                                        .addComponent(this.jLabelPkgResource)
                                                        .addComponent(this.jLabel1)
                                        )
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(
                                                jpSetupLayout
                                                        .createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(this.jScrollPane1, -1, 424, 32767)
                                                        .addComponent(this.jTextFieldServletName, GroupLayout.Alignment.TRAILING, -1, 424, 32767)
                                                        .addComponent(this.jComboBoxURLPattern, GroupLayout.Alignment.TRAILING, 0, 424, 32767)
                                                        .addComponent(this.jTextFieldAppResource, -1, 424, 32767)
                                                        .addComponent(this.jTextFieldWebPageResource, -1, 424, 32767)
                                                        .addComponent(this.jTextFieldPkgResource, -1, 424, 32767)
                                        )
                        )
        );
        jpSetupLayout.setVerticalGroup(
                jpSetupLayout
                        .createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(
                                jpSetupLayout
                                        .createSequentialGroup()
                                        .addGroup(
                                                jpSetupLayout
                                                        .createParallelGroup(GroupLayout.Alignment.CENTER)
                                                        .addComponent(this.jLabelServletName)
                                                        .addComponent(this.jTextFieldServletName, -2, -1, -2)
                                        )
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(
                                                jpSetupLayout
                                                        .createParallelGroup(GroupLayout.Alignment.CENTER)
                                                        .addComponent(this.jLabelURLPattern)
                                                        .addComponent(this.jComboBoxURLPattern, -2, -1, -2)
                                        )
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(
                                                jpSetupLayout
                                                        .createParallelGroup(GroupLayout.Alignment.CENTER)
                                                        .addComponent(this.jLabelAppResource)
                                                        .addComponent(this.jTextFieldAppResource, -2, -1, -2)
                                        )
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(
                                                jpSetupLayout
                                                        .createParallelGroup(GroupLayout.Alignment.CENTER)
                                                        .addComponent(this.jLabelWicketHomePage)
                                                        .addComponent(this.jTextFieldWebPageResource, -2, -1, -2)
                                        )
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(
                                                jpSetupLayout
                                                        .createParallelGroup(GroupLayout.Alignment.CENTER)
                                                        .addComponent(this.jLabelPkgResource)
                                                        .addComponent(this.jTextFieldPkgResource, -2, -1, -2)
                                        )
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(
                                                jpSetupLayout
                                                        .createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(this.jLabel1)
                                                        .addComponent(this.jScrollPane1, -2, 84, -2)
                                        )
                                        .addContainerGap(57, 32767)
                        )
        );
        jpSetupLayout.linkSize(SwingConstants.VERTICAL, this.jTextFieldAppResource, this.jTextFieldPkgResource);
        ResourceBundle bundle = ResourceBundle.getBundle("org/netbeans/modules/web/wicket/framework/Bundle");
        this.jTextFieldServletName.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_jTextFieldServletName"));
        this.jComboBoxURLPattern.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_jComboBoxURLPattern"));
        this.jTextFieldAppResource.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_jTextFieldAppResource"));
        this.add((Component)this.jpSetup, "card2");
        this.jtFolder.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent evt) {
                WicketConfigurationPanelVisual_without_form.this.jtFolderKeyPressed(evt);
            }
        });
        this.jbBrowse.setText(bundle.getString("LBL_Browse"));
        this.jbBrowse.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                WicketConfigurationPanelVisual_without_form.this.jbBrowseActionPerformed(evt);
            }
        });
        this.jTextArea1.setEditable(false);
        this.jTextArea1.setFont(new Font("Dialog", 1, 12));
        this.jTextArea1.setLineWrap(true);
        this.jTextArea1.setText(bundle.getString("MSG_Missing_Library"));
        this.jTextArea1.setWrapStyleWord(true);
        this.jTextArea1.setAutoscrolls(false);
        this.jTextArea1.setOpaque(false);
        this.jTextArea2.setEditable(false);
        this.jTextArea2.setLineWrap(true);
        this.jTextArea2.setText(bundle.getString("MSG_Missing_Library_Result"));
        this.jTextArea2.setWrapStyleWord(true);
        this.jTextArea2.setAutoscrolls(false);
        this.jTextArea2.setOpaque(false);
        GroupLayout jpLibraryLayout = new GroupLayout(this.jpLibrary);
        this.jpLibrary.setLayout(jpLibraryLayout);
        jpLibraryLayout.setHorizontalGroup(
                jpLibraryLayout
                        .createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(this.jTextArea1)
                        .addGroup(GroupLayout.Alignment.TRAILING,
                                jpLibraryLayout
                                        .createSequentialGroup()
                                        .addComponent(this.jtFolder, -1, 458, 32767)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(this.jbBrowse)
                        )
                        .addComponent(this.jTextArea2)
        );
        jpLibraryLayout.setVerticalGroup(
                jpLibraryLayout
                        .createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(
                                jpLibraryLayout
                                        .createSequentialGroup()
                                        .addComponent(this.jTextArea1, -2, -1, -2)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(
                                                jpLibraryLayout
                                                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(this.jbBrowse)
                                                        .addComponent(this.jtFolder, -2, -1, -2)
                                        )
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(this.jTextArea2, -2, -1, -2)
                                        .addContainerGap(522, 32767)
                        )
        );
        this.add((Component)this.jpLibrary, "card2");
    }

    private void jbBrowseActionPerformed(ActionEvent evt) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(NbBundle.getMessage(WicketConfigurationPanelVisual_without_form.class, (String)"LBL_SelectLibraryLocation"));
        chooser.setFileSelectionMode(1);
        if (0 == chooser.showOpenDialog(this)) {
            File projectDir = chooser.getSelectedFile();
            this.jtFolder.setText(projectDir.getAbsolutePath());
            this.checkFolderInstallation();
        }
    }

    private void jtFolderKeyPressed(KeyEvent evt) {
        this.checkFolderInstallation();
    }

    private void checkFolderInstallation() {
        File folder = new File(this.jtFolder.getText());
        if (!WicketConfigUtilities.isWicketInstallFolder(folder)) {
            this.panel.setErrorMessage(NbBundle.getMessage(WicketConfigurationPanelVisual_without_form.class, (String)"MSG_PathIsNotWicketFolder"));
        } else {
            this.panel.setErrorMessage(null);
            this.jpLibrary.setVisible(false);
            this.jpSetup.setVisible(true);
        }
    }

    private void refreshPanels() {
        for (Library library : LibraryManager.getDefault().getLibraries()) {
            if (library.getName().startsWith("Wicket")) {
                this.jpLibrary.setVisible(true);
                this.jpSetup.setVisible(false);
                continue;
            }
            this.jpLibrary.setVisible(false);
            this.jpSetup.setVisible(true);
        }
    }

    public File getInstallFolder() {
        File folder = new File(this.jtFolder.getText());
        return folder;
    }

    boolean valid(WizardDescriptor wizardDescriptor) {
        if (wizardDescriptor != null) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", null);
        }
        return true;
    }

    void validate(WizardDescriptor d) throws WizardValidationException {
    }

    void read(WizardDescriptor d) {
    }

    void store(WizardDescriptor d) {
    }

    void enableComponents(boolean enable) {
        this.jComboBoxURLPattern.setEnabled(enable);
        this.jTextFieldAppResource.setEnabled(enable);
        this.jTextFieldPkgResource.setEnabled(enable);
        this.jTextFieldServletName.setEnabled(enable);
        this.jLabelAppResource.setEnabled(enable);
        this.jLabelServletName.setEnabled(enable);
        this.jLabelURLPattern.setEnabled(enable);
        this.jTextFieldWebPageResource.setEnabled(enable);
        this.jLabelWicketHomePage.setEnabled(enable);
        this.jLabelPkgResource.setEnabled(enable);
    }

    public String getURLPattern() {
        String result = (String)this.jComboBoxURLPattern.getSelectedItem();
        if (result != null && !result.endsWith("/*")) {
            result = result.endsWith("/") ? result + "*" : result + "/*";
        }
        return result;
    }

    public void setURLPattern(String pattern) {
        this.jComboBoxURLPattern.setSelectedItem(pattern);
    }

    public String getWicketVersion() {
        return (String)this.versionList.getSelectedValue();
    }

    public String getServletName() {
        return this.jTextFieldServletName.getText();
    }

    public void setServletName(String name) {
        this.jTextFieldServletName.setText(name);
    }

    public String getAppResource() {
        return this.jTextFieldAppResource.getText();
    }

    public void setAppResource(String resource) {
        this.jTextFieldAppResource.setText(resource);
    }

    public String getPkgResource() {
        return this.jTextFieldPkgResource.getText();
    }

    public void setPkgResource(String resource) {
        this.jTextFieldPkgResource.setText(resource);
    }

    public String getWebPageResource() {
        return this.jTextFieldWebPageResource.getText();
    }

    public void setWebPageResource(String resource) {
        this.jTextFieldWebPageResource.setText(resource);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.web.wicket.framework.WicketConfigurationPanelVisual");
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        this.panel.fireChangeEvent();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        this.panel.fireChangeEvent();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        this.panel.fireChangeEvent();
    }

    private String parseFile(InputStream is) {
        String name = "---";
        try {
            org.w3c.dom.Document doc = XMLUtil.parse((InputSource)new InputSource(is), (boolean)true, (boolean)true, null, null);
            NodeList allNodes = doc.getElementsByTagName("*");
            for (int i = 0; i < allNodes.getLength(); ++i) {
                Node oneNode = allNodes.item(i);
                if (!oneNode.getNodeName().equals("name")) {
                    continue;
                }
                name = oneNode.getTextContent();
            }
            is.close();
        } catch (IOException | SAXException ex) {
            Exceptions.printStackTrace((Throwable)ex);
        }
        return name;
    }

}
