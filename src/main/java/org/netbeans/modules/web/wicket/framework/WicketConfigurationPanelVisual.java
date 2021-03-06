/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.framework;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
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
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.awt.Mnemonics;
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

/**
 *
 * @author Tim Boudreau
 */
public class WicketConfigurationPanelVisual extends JPanel implements HelpCtx.Provider, DocumentListener {

    private static final long serialVersionUID = 1L;

    private final WicketWebModuleExtender extender;
    private final WicketFrameworkProvider framework;

    /**
     * Creates new form WicketConfigurationPanelVisual
     *
     * @param extender Extends the panel by a status line etc.
     * @param framework Provides access to Wicket and its configuration
     * @param enableComponents Flag indicating if the components should be
     * enabled
     */
    public WicketConfigurationPanelVisual(WicketWebModuleExtender extender, WicketFrameworkProvider framework, boolean enableComponents) {
        this.extender = extender;
        this.framework = framework;
        initComponents();
        this.refreshPanels();
        this.jTextFieldAppResource.getDocument().addDocumentListener(this);
        ((JTextComponent)jComboBoxURLPattern.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);
        this.enableComponents(enableComponents);
        if (versionList.getModel().getSize() > 0) {
            this.versionList.setSelectedIndex(0);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jpSetup = new JPanel();
        jLabelServletName = new JLabel();
        jTextFieldServletName = new JTextField();
        jComboBoxURLPattern = new JComboBox<>();
        jLabelURLPattern = new JLabel();
        jLabelAppResource = new JLabel();
        jTextFieldAppResource = new JTextField();
        jLabelWicketHomePage = new JLabel();
        jTextFieldWebPageResource = new JTextField();
        jLabelPkgResource = new JLabel();
        jTextFieldPkgResource = new JTextField();
        jLabel1 = new JLabel();
        jScrollPane1 = new JScrollPane();
        versionList = new JList<>();
        jpLibrary = new JPanel();
        jTextArea1 = new JTextArea();
        jtFolder = new JTextField();
        jbBrowse = new JButton();
        jTextArea2 = new JTextArea();

        setLayout(new CardLayout());

        jLabelServletName.setLabelFor(jTextFieldServletName);
        Mnemonics.setLocalizedText(jLabelServletName, NbBundle.getMessage(WicketConfigurationPanelVisual.class, "WicketConfigurationPanelVisual.jLabelServletName.text")); // NOI18N

        jTextFieldServletName.setText(NbBundle.getMessage(WicketConfigurationPanelVisual.class, "WicketConfigurationPanelVisual.jTextFieldServletName.text")); // NOI18N

        jComboBoxURLPattern.setModel(new DefaultComboBoxModel<>(new String[] { "/wicket/*" }));

        jLabelURLPattern.setLabelFor(jComboBoxURLPattern);
        Mnemonics.setLocalizedText(jLabelURLPattern, NbBundle.getMessage(WicketConfigurationPanelVisual.class, "WicketConfigurationPanelVisual.jLabelURLPattern.text")); // NOI18N

        jLabelAppResource.setLabelFor(jTextFieldAppResource);
        Mnemonics.setLocalizedText(jLabelAppResource, NbBundle.getMessage(WicketConfigurationPanelVisual.class, "WicketConfigurationPanelVisual.jLabelAppResource.text")); // NOI18N

        jLabelWicketHomePage.setLabelFor(jTextFieldWebPageResource);
        Mnemonics.setLocalizedText(jLabelWicketHomePage, NbBundle.getMessage(WicketConfigurationPanelVisual.class, "WicketConfigurationPanelVisual.jLabelWicketHomePage.text")); // NOI18N

        jLabelPkgResource.setLabelFor(jTextFieldPkgResource);
        Mnemonics.setLocalizedText(jLabelPkgResource, NbBundle.getMessage(WicketConfigurationPanelVisual.class, "WicketConfigurationPanelVisual.jLabelPkgResource.text")); // NOI18N

        Mnemonics.setLocalizedText(jLabel1, NbBundle.getMessage(WicketConfigurationPanelVisual.class, "WicketConfigurationPanelVisual.jLabel1.text")); // NOI18N

        versionList.setModel(createWicketLibrariesListModel());
        versionList.setDropMode(DropMode.ON);
        jScrollPane1.setViewportView(versionList);

        GroupLayout jpSetupLayout = new GroupLayout(jpSetup);
        jpSetup.setLayout(jpSetupLayout);
        jpSetupLayout.setHorizontalGroup(jpSetupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jpSetupLayout.createSequentialGroup()
                .addGroup(jpSetupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelAppResource)
                    .addComponent(jLabelServletName)
                    .addComponent(jLabelURLPattern)
                    .addComponent(jLabelWicketHomePage)
                    .addComponent(jLabelPkgResource)
                    .addComponent(jLabel1))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jpSetupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jTextFieldPkgResource, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 585, Short.MAX_VALUE)
                    .addComponent(jTextFieldAppResource, GroupLayout.Alignment.TRAILING)
                    .addComponent(jTextFieldServletName, GroupLayout.Alignment.TRAILING)
                    .addComponent(jComboBoxURLPattern, GroupLayout.Alignment.TRAILING, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextFieldWebPageResource)
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );
        jpSetupLayout.setVerticalGroup(jpSetupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jpSetupLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpSetupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelAppResource)
                    .addComponent(jTextFieldAppResource, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jpSetupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelServletName)
                    .addComponent(jTextFieldServletName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jpSetupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelURLPattern)
                    .addComponent(jComboBoxURLPattern, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jpSetupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelWicketHomePage)
                    .addComponent(jTextFieldWebPageResource, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jpSetupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelPkgResource)
                    .addComponent(jTextFieldPkgResource))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jpSetupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jpSetupLayout.linkSize(SwingConstants.VERTICAL, new Component[] {jLabelAppResource, jLabelPkgResource});

        jTextFieldServletName.getAccessibleContext().setAccessibleName(NbBundle.getMessage(WicketConfigurationPanelVisual.class, "WicketConfigurationPanelVisual.jTextFieldServletName.AccessibleContext.accessibleName")); // NOI18N
        jComboBoxURLPattern.getAccessibleContext().setAccessibleName(NbBundle.getMessage(WicketConfigurationPanelVisual.class, "WicketConfigurationPanelVisual.jComboBoxURLPattern.AccessibleContext.accessibleName")); // NOI18N
        jTextFieldAppResource.getAccessibleContext().setAccessibleName(NbBundle.getMessage(WicketConfigurationPanelVisual.class, "WicketConfigurationPanelVisual.jTextFieldAppResource.AccessibleContext.accessibleName")); // NOI18N

        add(jpSetup, "card2");

        jTextArea1.setEditable(false);
        jTextArea1.setFont(new Font("Dialog", 1, 12)); // NOI18N
        jTextArea1.setText(NbBundle.getMessage(WicketConfigurationPanelVisual.class, "WicketConfigurationPanelVisual.jTextArea1.text")); // NOI18N
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setAutoscrolls(false);
        jTextArea1.setOpaque(false);

        jtFolder.setPreferredSize(new Dimension(458, 19));
        jtFolder.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                jtFolderKeyPressed(evt);
            }
        });

        Mnemonics.setLocalizedText(jbBrowse, NbBundle.getMessage(WicketConfigurationPanelVisual.class, "WicketConfigurationPanelVisual.jbBrowse.text")); // NOI18N
        jbBrowse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jbBrowseActionPerformed(evt);
            }
        });

        jTextArea2.setEditable(false);
        jTextArea2.setLineWrap(true);
        jTextArea2.setText(NbBundle.getMessage(WicketConfigurationPanelVisual.class, "WicketConfigurationPanelVisual.jTextArea2.text")); // NOI18N
        jTextArea2.setWrapStyleWord(true);
        jTextArea2.setAutoscrolls(false);
        jTextArea2.setOpaque(false);

        GroupLayout jpLibraryLayout = new GroupLayout(jpLibrary);
        jpLibrary.setLayout(jpLibraryLayout);
        jpLibraryLayout.setHorizontalGroup(jpLibraryLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(jTextArea1)
            .addGroup(jpLibraryLayout.createSequentialGroup()
                .addComponent(jtFolder, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jbBrowse))
            .addComponent(jTextArea2)
        );
        jpLibraryLayout.setVerticalGroup(jpLibraryLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jpLibraryLayout.createSequentialGroup()
                .addComponent(jTextArea1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jpLibraryLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jbBrowse)
                    .addComponent(jtFolder, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextArea2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(261, Short.MAX_VALUE))
        );

        add(jpLibrary, "card3");
    }// </editor-fold>//GEN-END:initComponents

    private void jtFolderKeyPressed(KeyEvent evt) {//GEN-FIRST:event_jtFolderKeyPressed
        this.checkFolderInstallation();
    }//GEN-LAST:event_jtFolderKeyPressed

    private void jbBrowseActionPerformed(ActionEvent evt) {//GEN-FIRST:event_jbBrowseActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(NbBundle.getMessage(WicketConfigurationPanelVisual.class, "LBL_SelectLibraryLocation"));
        chooser.setFileSelectionMode(1);
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File projectDir = chooser.getSelectedFile();
            jtFolder.setText(projectDir.getAbsolutePath());
            checkFolderInstallation();
        }
    }//GEN-LAST:event_jbBrowseActionPerformed

    private void checkFolderInstallation() {
        File folder = new File(jtFolder.getText());
        if (!WicketConfigUtilities.isWicketInstallFolder(folder)) {
            extender.setErrorMessage(NbBundle.getMessage(WicketConfigurationPanelVisual.class, "MSG_PathIsNotWicketFolder"));
        } else {
            try {
                extender.setErrorMessage(null);
                framework.addLibrary(jtFolder.getText());
                versionList.setModel(createWicketLibrariesListModel());
                if (versionList.getModel().getSize() > 0) {
                    this.versionList.setSelectedIndex(0);
                }
                refreshPanels();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private void refreshPanels() {
        jpLibrary.setVisible(true);
        jpSetup.setVisible(false);
        for (Library library : LibraryManager.getDefault().getLibraries()) {
            if (library.getName().startsWith("Wicket")) {
                jpLibrary.setVisible(false);
                jpSetup.setVisible(true);
                break;
            }
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
        return versionList.getSelectedValue();
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
        this.extender.fireChangeEvent();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        this.extender.fireChangeEvent();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        this.extender.fireChangeEvent();
    }

    private String parseFile(InputStream is) {
        String name = "---";
        try {
            org.w3c.dom.Document doc = XMLUtil.parse(new InputSource(is), true, true, null, null);
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

    private ListModel<String> createWicketLibrariesListModel() {
        DefaultListModel<String> librariesListModel = new DefaultListModel<>();
        FileObject versionFo = FileUtil.getConfigFile("org-netbeans-api-project-libraries/Libraries");
        if (versionFo != null) {
            FileObject[] versionKids = versionFo.getChildren();
            for (FileObject versionKid : FileUtil.getOrder(Arrays.asList(versionKids), (boolean)true)) {
                if (versionKid.getName().startsWith("Wicket")) {
                    try {
                        String libraryName = this.parseFile(versionKid.getInputStream());
                        librariesListModel.add(0, libraryName);
                    } catch (FileNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
        return librariesListModel;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JComboBox<String> jComboBoxURLPattern;
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
    private JList<String> versionList;
    // End of variables declaration//GEN-END:variables
}
