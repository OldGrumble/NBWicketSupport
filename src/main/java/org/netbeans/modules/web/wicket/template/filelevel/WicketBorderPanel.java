/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.template.filelevel;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 *
 * @author Tim Boudreau
 */
final class WicketBorderPanel implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel {

    private final Project project;
    private final Set listeners = new HashSet(1);
    private WizardDescriptor wizardDescriptor;
    private WicketBorderPanelVisual component;

    public WicketBorderPanel(Project project, WizardDescriptor wizardDescriptor) {
        this.project = project;
        this.wizardDescriptor = wizardDescriptor;
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }

    @Override
    public Component getComponent() {
        if (this.component == null) {
            this.component = new WicketBorderPanelVisual(this.project);
        }
        return this.component;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.web.wicket.template.filelevel.WicketPagePanel");
    }

    @Override
    public boolean isValid() {
        this.getComponent();
        return this.component.valid(this.wizardDescriptor);
    }

    @Override
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    protected final void fireChangeEvent() {
        Iterator it;
        synchronized (listeners) {
            it = (new HashSet(listeners)).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }

    @Override
    public void readSettings(Object settings) {
        this.wizardDescriptor = (WizardDescriptor)settings;
        this.component.read(this.wizardDescriptor);
        Object substitute = this.component.getClientProperty("NewProjectWizard_Title");
        if (substitute != null) {
            this.wizardDescriptor.putProperty("NewProjectWizard_Title", substitute);
        }
    }

    @Override
    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor)settings;
        this.component.store(d);
        d.putProperty("NewProjectWizard_Title", null);
    }

    public String getSelectedSuperclass(String resource) {
        return component.getSelectedSuperclass();
    }
}
