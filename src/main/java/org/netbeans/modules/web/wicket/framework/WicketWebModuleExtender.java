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

    private final WicketFrameworkProvider framework;
    private final boolean isEnabled;
    private final ExtenderController controller;
    private WicketConfigurationPanelVisual component;
    private WizardDescriptor wizardDescriptor;
    private String error_message;
    private final Set listeners = new HashSet(1);

    public WicketWebModuleExtender(WicketFrameworkProvider framework, ExtenderController controller, boolean isEnabled) {
        this.framework = framework;
        this.controller = controller;
        this.isEnabled = isEnabled;
        getComponent();
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }

    @Override
    public WicketConfigurationPanelVisual getComponent() {
        if (component == null) {
            component = new WicketConfigurationPanelVisual(this, framework, isEnabled);
        }
        return this.component;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.web.wicket.framework.WicketWebModuleExtender");
    }

    @Override
    public boolean isValid() {
        this.getComponent();
        return component.valid(wizardDescriptor);
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }

    @Override
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor)settings;
        component.read(wizardDescriptor);
        Object substitute = component.getClientProperty("NewProjectWizard_Title");
        if (substitute != null) {
            wizardDescriptor.putProperty("NewProjectWizard_Title", substitute);
        }
    }

    @Override
    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor)settings;
        component.store(d);
        d.putProperty("NewProjectWizard_Title", null);
    }

    @Override
    public void validate() throws WizardValidationException {
        getComponent();
        component.validate(wizardDescriptor);
    }

    public void enableComponents(boolean enable) {
        this.getComponent();
        component.enableComponents(enable);
    }

    public String getURLPattern() {
        return component.getURLPattern();
    }

    public void setURLPattern(String pattern) {
        component.setURLPattern(pattern);
    }

    public String getWicketVersion() {
        return component.getWicketVersion();
    }

    public String getServletName() {
        return component.getServletName();
    }

    public void setServletName(String name) {
        component.setServletName(name);
    }

    public String getAppResource() {
        return component.getAppResource();
    }

    public void setAppResource(String resource) {
        component.setAppResource(resource);
    }

    public String getPkgResource() {
        return component.getPkgResource();
    }

    void setPkgResource(String resource) {
        component.setPkgResource(resource);
    }

    public String getWebPageResource() {
        return component.getWebPageResource();
    }

    void setWebPageResource(String resource) {
        component.setWebPageResource(resource);
    }

    public File getInstallFolder() {
        return component.getInstallFolder();
    }

    protected void setErrorMessage(String message) {
        if (error_message != null && (message == null || "".equals(message))) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", (Object)"");
            error_message = null;
        } else {
            error_message = message;
        }
        fireChangeEvent();
    }

    @Override
    public void update() {
    }

    @Override
    public Set extend(WebModule webModule) {
        return framework.extendImpl(webModule);
    }

    public ExtenderController getController() {
        return controller;
    }
}
