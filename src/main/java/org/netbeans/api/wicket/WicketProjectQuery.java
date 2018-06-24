/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.api.wicket;

import java.util.Collection;
import java.util.Iterator;
import org.netbeans.api.project.Project;
import org.netbeans.spi.wicket.WicketProjectQueryImplementation;
import org.openide.util.Lookup;

/**
 *
 * @author peter
 */
public class WicketProjectQuery {

    public static boolean isWicket(Project project) {
        Collection c = Lookup.getDefault().lookupAll(WicketProjectQueryImplementation.class);
        boolean result = false;
        Iterator iterator = c.iterator();
        do
                {
            if(!iterator.hasNext())
                break;
            WicketProjectQueryImplementation impl = (WicketProjectQueryImplementation)iterator.next();
            result = impl.isWicket(project);
                } while(!result);
        return result;
    }
}
