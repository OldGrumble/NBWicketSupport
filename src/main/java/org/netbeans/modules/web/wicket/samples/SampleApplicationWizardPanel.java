/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.samples;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Tim Boudreau
 */
public class SampleApplicationWizardPanel implements WizardDescriptor.Panel, WizardDescriptor.ValidatingPanel, WizardDescriptor.FinishablePanel {

    private WizardDescriptor wizardDescriptor;
    private SampleApplicationPanelVisual component;
    private final Set listeners = new HashSet(1);

    @Override
    public Component getComponent() {
        if (this.component == null) {
            this.component = new SampleApplicationPanelVisual(this);
            this.component.setName(NbBundle.getMessage(SampleApplicationWizardPanel.class, (String)"LBL_CreateProjectStep"));
        }
        return this.component;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.web.wicket.samples.SampleApplicationWizardPanel");
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
            it = new HashSet(listeners).iterator();
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
    }

    @Override
    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor)settings;
        this.component.store(d);
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }

    @Override
    public void validate() throws WizardValidationException {
        this.getComponent();
        this.component.validate(this.wizardDescriptor);
    }
}
