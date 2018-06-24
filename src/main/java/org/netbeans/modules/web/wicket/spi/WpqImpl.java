/*
 * Decompiled with CFR 0_130.
 * 
 * Could not load the following classes:
 *  org.netbeans.api.project.Project
 */
package org.netbeans.modules.web.wicket.spi;

import org.netbeans.api.project.Project;
import org.netbeans.spi.wicket.WicketProjectQueryImplementation;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = WicketProjectQueryImplementation.class)
public class WpqImpl implements WicketProjectQueryImplementation {

    @Override
    public boolean isWicket(Project project) {
        return true;
    }
}
