/*
 * Decompiled with CFR 0_130.
 * 
 * Could not load the following classes:
 *  org.netbeans.api.project.Project
 *  org.openide.WizardDescriptor
 *  org.openide.WizardDescriptor$FinishablePanel
 *  org.openide.WizardDescriptor$Panel
 *  org.openide.util.HelpCtx
 */
package org.netbeans.modules.web.wicket.template.filelevel;

import java.awt.Component;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.wicket.template.filelevel.WicketPagePanel;
import org.netbeans.modules.web.wicket.template.filelevel.WicketPanelPanelVisual;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

final class WicketPanelPanel implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel {

    private WizardDescriptor wizardDescriptor;
    private WicketPanelPanelVisual component;
    private Project project;
    private final Set listeners = new HashSet(1);

    public WicketPanelPanel(Project project, WizardDescriptor wizardDescriptor) {
        this.project = project;
        this.wizardDescriptor = wizardDescriptor;
    }

    public boolean isFinishPanel() {
        return true;
    }

    public Component getComponent() {
        if (this.component == null) {
            this.component = new WicketPanelPanelVisual(this.project);
        }
        return this.component;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(WicketPagePanel.class);
    }

    public boolean isValid() {
        this.getComponent();
        return this.component.valid(this.wizardDescriptor);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void addChangeListener(ChangeListener l) {
        Set set = this.listeners;
        synchronized (set) {
            this.listeners.add(l);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void removeChangeListener(ChangeListener l) {
        Set set = this.listeners;
        synchronized (set) {
            this.listeners.remove(l);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected final void fireChangeEvent() {
        Iterator it;
        Set set = this.listeners;
        synchronized (set) {
            it = new HashSet(this.listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }

    public void readSettings(Object settings) {
        this.wizardDescriptor = (WizardDescriptor)settings;
        this.component.read(this.wizardDescriptor);
        Object substitute = this.component.getClientProperty("NewProjectWizard_Title");
        if (substitute != null) {
            this.wizardDescriptor.putProperty("NewProjectWizard_Title", substitute);
        }
    }

    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor)settings;
        this.component.store(d);
        d.putProperty("NewProjectWizard_Title", null);
    }

    public String getjComboBoxSuperclass(String resource) {
        return (String)this.component.jComboBoxSuperclass.getEditor().getItem();
    }
}
