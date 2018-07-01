/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.api.wicket;

import java.util.Collection;
import java.util.Iterator;
import org.netbeans.api.project.Project;
import org.netbeans.spi.wicket.WicketProjectQueryImplementation;
import org.openide.util.Lookup;

/**
 *
 * @author Tim Boudreau
 */
public class WicketProjectQuery {

    public static boolean isWicket(Project project) {
        boolean result = false;
        Collection c = Lookup.getDefault().lookupAll(WicketProjectQueryImplementation.class);
        Iterator iterator = c.iterator();
        while (iterator.hasNext() && !(result = ((WicketProjectQueryImplementation)iterator.next()).isWicket(project))) {
        }
        return result;
    }
}
