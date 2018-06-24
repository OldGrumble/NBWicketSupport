/*
 * Decompiled with CFR 0_130.
 * 
 * Could not load the following classes:
 *  org.openide.WizardDescriptor
 *  org.openide.WizardDescriptor$FinishablePanel
 *  org.openide.WizardDescriptor$Panel
 *  org.openide.WizardDescriptor$ValidatingPanel
 *  org.openide.WizardValidationException
 *  org.openide.util.HelpCtx
 *  org.openide.util.NbBundle
 */
package org.netbeans.modules.web.wicket.samples;

import java.awt.Component;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.wicket.samples.SampleApplicationPanelVisual;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class SampleApplicationWizardPanel
implements WizardDescriptor.Panel,
WizardDescriptor.ValidatingPanel,
WizardDescriptor.FinishablePanel {
    private WizardDescriptor wizardDescriptor;
    private SampleApplicationPanelVisual component;
    private final Set listeners = new HashSet(1);

    public Component getComponent() {
        if (this.component == null) {
            this.component = new SampleApplicationPanelVisual(this);
            this.component.setName(NbBundle.getMessage(SampleApplicationWizardPanel.class, (String)"LBL_CreateProjectStep"));
        }
        return this.component;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(SampleApplicationWizardPanel.class);
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
    }

    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor)settings;
        this.component.store(d);
    }

    public boolean isFinishPanel() {
        return true;
    }

    public void validate() throws WizardValidationException {
        this.getComponent();
        this.component.validate(this.wizardDescriptor);
    }
}

