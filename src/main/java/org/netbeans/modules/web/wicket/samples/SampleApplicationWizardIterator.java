/*
 * Decompiled with CFR 0_130.
 * 
 * Could not load the following classes:
 *  org.netbeans.api.project.ProjectManager
 *  org.netbeans.spi.project.ui.support.ProjectChooser
 *  org.netbeans.spi.project.ui.templates.support.Templates
 *  org.openide.WizardDescriptor
 *  org.openide.WizardDescriptor$InstantiatingIterator
 *  org.openide.WizardDescriptor$Panel
 *  org.openide.filesystems.FileLock
 *  org.openide.filesystems.FileObject
 *  org.openide.filesystems.FileUtil
 *  org.openide.util.NbBundle
 */
package org.netbeans.modules.web.wicket.samples;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.api.templates.TemplateRegistrations;
import org.netbeans.modules.web.wicket.samples.SampleApplicationWizardPanel;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public class SampleApplicationWizardIterator
implements WizardDescriptor.InstantiatingIterator {
    private int index;
    private WizardDescriptor.Panel[] panels;
    private WizardDescriptor wiz;

    @TemplateRegistrations(value = {@TemplateRegistration(folder = "Project/Samples/Wicket", position = 1000, content = "/org/netbeans/modules/web/wicket/resources/Pizzas.zip", displayName = "org.netbeans.modules.web.wicket.resources.Bundle#Templates/Project/Samples/Wicket/Pizzas.zip", iconBase = "org/netbeans/modules/web/wicket/resources/wicket.gif", description = "/org/netbeans/modules/web/wicket/samples/PizzaApplicationDescription.html"), @TemplateRegistration(folder = "Project/Samples/Wicket", position = 1100, content = "/org/netbeans/modules/web/wicket/resources/Authentications.zip", displayName = "org.netbeans.modules.web.wicket.resources.Bundle#Templates/Project/Samples/Wicket/Authentications.zip", iconBase = "org/netbeans/modules/web/wicket/resources/wicket.gif", description = "/org/netbeans/modules/web/wicket/samples/LoginApplicationDescription.html"), @TemplateRegistration(folder = "Project/Samples/Wicket", position = 1200, content = "/org/netbeans/modules/web/wicket/resources/Tabs.zip", displayName = "org.netbeans.modules.web.wicket.resources.Bundle#Templates/Project/Samples/Wicket/Tabs.zip", iconBase = "org/netbeans/modules/web/wicket/resources/wicket.gif", description = "/org/netbeans/modules/web/wicket/samples/TabbedApplicationDescription.html")})
    public static SampleApplicationWizardIterator createIterator() {
        return new SampleApplicationWizardIterator();
    }

    private WizardDescriptor.Panel[] createPanels() {
        FileObject template = Templates.getTemplate((WizardDescriptor)this.wiz);
        return new WizardDescriptor.Panel[]{new SampleApplicationWizardPanel()};
    }

    private String[] createSteps() {
        return new String[]{NbBundle.getMessage(SampleApplicationWizardIterator.class, (String)"LBL_CreateProjectStep")};
    }

    public Set instantiate() throws IOException {
        LinkedHashSet<FileObject> resultSet = new LinkedHashSet<FileObject>();
        File dirF = FileUtil.normalizeFile((File)((File)this.wiz.getProperty("projdir")));
        dirF.mkdirs();
        FileObject template = Templates.getTemplate((WizardDescriptor)this.wiz);
        FileObject dir = FileUtil.toFileObject((File)dirF);
        SampleApplicationWizardIterator.unZipFile(template.getInputStream(), dir);
        resultSet.add(dir);
        Enumeration e = dir.getFolders(true);
        while (e.hasMoreElements()) {
            FileObject subfolder = (FileObject)e.nextElement();
            if (!ProjectManager.getDefault().isProject(subfolder)) continue;
            resultSet.add(subfolder);
        }
        File parent = dirF.getParentFile();
        if (parent != null && parent.exists()) {
            ProjectChooser.setProjectsFolder((File)parent);
        }
        return resultSet;
    }

    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        this.index = 0;
        this.panels = this.createPanels();
        String[] steps = this.createSteps();
        for (int i = 0; i < this.panels.length; ++i) {
            Component c = this.panels[i].getComponent();
            if (steps[i] == null) {
                steps[i] = c.getName();
            }
            if (!(c instanceof JComponent)) continue;
            JComponent jc = (JComponent)c;
            jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
            jc.putClientProperty("WizardPanel_contentData", steps);
        }
    }

    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty("projdir", null);
        this.wiz.putProperty("name", null);
        this.wiz = null;
        this.panels = null;
    }

    public String name() {
        return MessageFormat.format("{0} of {1}", new Integer(this.index + 1), new Integer(this.panels.length));
    }

    public boolean hasNext() {
        return this.index < this.panels.length - 1;
    }

    public boolean hasPrevious() {
        return this.index > 0;
    }

    public void nextPanel() {
        if (!this.hasNext()) {
            throw new NoSuchElementException();
        }
        ++this.index;
    }

    public void previousPanel() {
        if (!this.hasPrevious()) {
            throw new NoSuchElementException();
        }
        --this.index;
    }

    public WizardDescriptor.Panel current() {
        return this.panels[this.index];
    }

    public final void addChangeListener(ChangeListener l) {
    }

    public final void removeChangeListener(ChangeListener l) {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void unZipFile(InputStream source, FileObject projectRoot) throws IOException {
        try {
            ZipEntry entry;
            ZipInputStream str = new ZipInputStream(source);
            while ((entry = str.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    FileUtil.createFolder((FileObject)projectRoot, (String)entry.getName());
                    continue;
                }
                FileObject fo = FileUtil.createData((FileObject)projectRoot, (String)entry.getName());
                FileLock lock = fo.lock();
                try {
                    OutputStream out = fo.getOutputStream(lock);
                    try {
                        FileUtil.copy((InputStream)str, (OutputStream)out);
                    }
                    finally {
                        out.close();
                    }
                }
                finally {
                    lock.releaseLock();
                }
            }
        }
        finally {
            source.close();
        }
    }
}

