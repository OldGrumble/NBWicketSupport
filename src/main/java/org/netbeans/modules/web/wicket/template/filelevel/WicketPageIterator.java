/*
 * Decompiled with CFR 0_130.
 * 
 * Could not load the following classes:
 *  org.netbeans.api.java.classpath.ClassPath
 *  org.netbeans.api.project.Project
 *  org.netbeans.api.project.ProjectUtils
 *  org.netbeans.api.project.SourceGroup
 *  org.netbeans.spi.java.project.support.ui.templates.JavaTemplates
 *  org.netbeans.spi.project.ui.templates.support.Templates
 *  org.openide.WizardDescriptor
 *  org.openide.WizardDescriptor$Panel
 *  org.openide.cookies.EditorCookie
 *  org.openide.cookies.SaveCookie
 *  org.openide.filesystems.FileObject
 *  org.openide.loaders.DataFolder
 *  org.openide.loaders.DataObject
 *  org.openide.loaders.TemplateWizard
 *  org.openide.loaders.TemplateWizard$Iterator
 *  org.openide.nodes.Node
 *  org.openide.nodes.Node$Cookie
 *  org.openide.util.NbBundle
 */
package org.netbeans.modules.web.wicket.template.filelevel;

import java.awt.Component;
import java.io.IOException;
import java.io.PrintStream;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.web.wicket.template.filelevel.WicketPagePanel;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

public class WicketPageIterator implements TemplateWizard.Iterator {

    private int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient boolean debug = false;
    private static String htmltext = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n\n<html>\n  <head>\n    <title></title>\n  </head>\n  <body>\n\n  </body>\n</html>";
    private static String javatext = "/*\n * __CLASS__.java\n *\n * Created on March 19, 2006, 1:13 PM\n *\n */\n\npackage __PACKAGE__;\n\nimport __FULL_SUPERCLASS__;\n\npublic class __CLASS__ extends __SUPERCLASS__ {\n\n    /** Creates a new instance of __CLASS__ */\n    public __CLASS__() {\n\n    }\n\n}";

    public static WicketPageIterator createIterator() {
        return new WicketPageIterator();
    }

    public void initialize(TemplateWizard wizard) {
        if (this.debug) {
            this.log("initialize");
        }
        this.index = 0;
        Project project = Templates.getProject((WizardDescriptor)wizard);
        DataFolder targetFolder = null;
        try {
            targetFolder = wizard.getTargetFolder();
        } catch (IOException ex) {
            targetFolder = DataFolder.findFolder((FileObject)project.getProjectDirectory());
        }
        SourceGroup[] sourceGroups = ProjectUtils.getSources((Project)project).getSourceGroups("java");
        if (this.debug) {
            this.log("\tproject: " + (Object)project);
            this.log("\ttargetFolder: " + (Object)targetFolder);
            this.log("\tsourceGroups.length: " + sourceGroups.length);
        }
        WicketPagePanel secondPanel = new WicketPagePanel(project, (WizardDescriptor)wizard);
        this.panels = new WizardDescriptor.Panel[]{JavaTemplates.createPackageChooser((Project)project, (SourceGroup[])sourceGroups, (WizardDescriptor.Panel)secondPanel)};
        Object prop = wizard.getProperty("WizardPanel_contentData");
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = this.createSteps(beforeSteps, this.panels);
        for (int i = 0; i < this.panels.length; ++i) {
            JComponent jc = (JComponent)this.panels[i].getComponent();
            if (steps[i] == null) {
                steps[i] = jc.getName();
            }
            jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
            jc.putClientProperty("WizardPanel_contentData", steps);
        }
    }

    public void uninitialize(TemplateWizard wizard) {
        this.panels = null;
    }

    public Set instantiate(TemplateWizard wizard) throws IOException {
        if (this.debug) {
            this.log("instantiate");
        }
        FileObject dir = Templates.getTargetFolder((WizardDescriptor)wizard);
        DataFolder df = DataFolder.findFolder((FileObject)dir);
        FileObject template = Templates.getTemplate((WizardDescriptor)wizard);
        DataObject dTemplate = DataObject.find((FileObject)template);
        DataObject HTMLobj = dTemplate.createFromTemplate(df, Templates.getTargetName((WizardDescriptor)wizard) + ".html");
        DataObject JAVAobj = dTemplate.createFromTemplate(df, Templates.getTargetName((WizardDescriptor)wizard) + ".java");
        EditorCookie editorCookie = (EditorCookie)HTMLobj.getCookie(EditorCookie.class);
        if (editorCookie != null) {
            StyledDocument doc = editorCookie.openDocument();
            this.replaceInDocument(doc, "__CONTENT__", htmltext);
            SaveCookie save = (SaveCookie)HTMLobj.getCookie(SaveCookie.class);
            if (save != null) {
                save.save();
            }
        }
        DataFolder targetFolder = wizard.getTargetFolder();
        FileObject folder = targetFolder.getPrimaryFile();
        ClassPath cp = ClassPath.getClassPath((FileObject)folder, (String)"classpath/source");
        String fullTarget = cp.getResourceName(folder, '.', false);
        String fullSuperClass = (String)wizard.getProperty("wicketPageSuperclass");
        String superClass = fullSuperClass.substring(fullSuperClass.lastIndexOf(".") + 1);
        EditorCookie editorCookie1 = (EditorCookie)JAVAobj.getCookie(EditorCookie.class);
        if (editorCookie1 != null) {
            StyledDocument doc = editorCookie1.openDocument();
            this.replaceInDocument(doc, "__CONTENT__", javatext);
            this.replaceInDocument(doc, "__FULL_SUPERCLASS__", (String)wizard.getProperty("wicketPageSuperclass"));
            this.replaceInDocument(doc, "__CLASS__", wizard.getTargetName());
            this.replaceInDocument(doc, "__PACKAGE__", fullTarget);
            this.replaceInDocument(doc, "__SUPERCLASS__", superClass);
            SaveCookie save = (SaveCookie)JAVAobj.getCookie(SaveCookie.class);
            if (save != null) {
                save.save();
            }
        }
        return null;
    }

    public void previousPanel() {
        if (!this.hasPrevious()) {
            throw new NoSuchElementException();
        }
        --this.index;
    }

    public void nextPanel() {
        if (!this.hasNext()) {
            throw new NoSuchElementException();
        }
        ++this.index;
    }

    public boolean hasPrevious() {
        return this.index > 0;
    }

    public boolean hasNext() {
        return this.index < this.panels.length - 1;
    }

    public String name() {
        return NbBundle.getMessage(WicketPageIterator.class, (String)"TITLE_x_of_y", (Object)new Integer(this.index + 1), (Object)new Integer(this.panels.length));
    }

    public WizardDescriptor.Panel current() {
        return this.panels[this.index];
    }

    public final void addChangeListener(ChangeListener l) {
    }

    public final void removeChangeListener(ChangeListener l) {
    }

    private void log(String message) {
        System.out.println("ActionIterator:: \t" + message);
    }

    private String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        int diff = 0;
        if (before == null) {
            before = new String[]{};
        } else if (before.length > 0) {
            diff = "...".equals(before[before.length - 1]) ? 1 : 0;
        }
        String[] res = new String[before.length - diff + panels.length];
        for (int i = 0; i < res.length; ++i) {
            res[i] = i < before.length - diff ? before[i] : panels[i - before.length + diff].getComponent().getName();
        }
        return res;
    }

    private void replaceInDocument(Document document, String replaceFrom, String replaceTo) {
        AbstractDocument doc = (AbstractDocument)document;
        int len = replaceFrom.length();
        try {
            String content = doc.getText(0, doc.getLength());
            int index = content.lastIndexOf(replaceFrom);
            while (index >= 0) {
                doc.replace(index, len, replaceTo, null);
                content = content.substring(0, index);
                index = content.lastIndexOf(replaceFrom);
            }
        } catch (BadLocationException ex) {
            // empty catch block
        }
    }
}
