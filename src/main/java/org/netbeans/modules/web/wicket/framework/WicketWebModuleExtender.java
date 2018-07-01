/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.framework;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

/**
 *
 * @author Tim Boudreau
 */
public final class WicketWebModuleExtender extends WebModuleExtender implements WizardDescriptor.FinishablePanel, WizardDescriptor.ValidatingPanel {

    private WicketFrameworkProvider framework;
    private final ExtenderController controller;
    private WicketConfigurationPanelVisual component;
    private WizardDescriptor wizardDescriptor;
    private String error_message;
    private final boolean isEnabled;
    private final Set listeners = new HashSet(1);

    public WicketWebModuleExtender(WicketFrameworkProvider framework, ExtenderController controller, boolean isEnabled) {
        this.framework = framework;
        this.controller = controller;
        this.isEnabled = isEnabled;
        this.getComponent();
    }

    public boolean isFinishPanel() {
        return true;
    }

    public WicketConfigurationPanelVisual getComponent() {
        if (this.component == null) {
            this.component = new WicketConfigurationPanelVisual(this, this.framework, this.isEnabled);
        }
        return this.component;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(WicketWebModuleExtender.class);
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
        ChangeEvent ev = new ChangeEvent((Object)this);
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

    public void validate() throws WizardValidationException {
        this.getComponent();
        this.component.validate(this.wizardDescriptor);
    }

    public void enableComponents(boolean enable) {
        this.getComponent();
        this.component.enableComponents(enable);
    }

    public String getURLPattern() {
        return this.component.getURLPattern();
    }

    public void setURLPattern(String pattern) {
        this.component.setURLPattern(pattern);
    }

    public String getWicketVersion() {
        return this.component.getWicketVersion();
    }

    public String getServletName() {
        return this.component.getServletName();
    }

    public void setServletName(String name) {
        this.component.setServletName(name);
    }

    public String getAppResource() {
        return this.component.getAppResource();
    }

    public void setAppResource(String resource) {
        this.component.setAppResource(resource);
    }

    public String getPkgResource() {
        return this.component.getPkgResource();
    }

    void setPkgResource(String resource) {
        this.component.setPkgResource(resource);
    }

    public String getWebPageResource() {
        return this.component.getWebPageResource();
    }

    void setWebPageResource(String resource) {
        this.component.setWebPageResource(resource);
    }

    public File getInstallFolder() {
        return this.component.getInstallFolder();
    }

    protected void setErrorMessage(String message) {
        if (this.error_message != null && (message == null || "".equals(message))) {
            this.wizardDescriptor.putProperty("WizardPanel_errorMessage", (Object)"");
            this.error_message = null;
        } else {
            this.error_message = message;
        }
        this.fireChangeEvent();
    }

    public void update() {
    }

    public Set extend(WebModule webModule) {
        return this.framework.extendImpl(webModule);
    }

    public ExtenderController getController() {
        return this.controller;
    }
}
