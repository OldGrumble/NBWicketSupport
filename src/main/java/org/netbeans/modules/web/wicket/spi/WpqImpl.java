/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.spi;

import org.netbeans.api.project.Project;
import org.netbeans.spi.wicket.WicketProjectQueryImplementation;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tim Boudreau
 */
@ServiceProvider(service = WicketProjectQueryImplementation.class)
public class WpqImpl implements WicketProjectQueryImplementation {

    @Override
    public boolean isWicket(Project project) {
        return true;
    }
}
