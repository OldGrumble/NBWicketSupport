/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.api.wicket;

import java.util.Collection;
import java.util.Iterator;
import org.netbeans.spi.wicket.JavaForMarkupQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Gj
 */
public final class JavaForMarkupQuery {

    private JavaForMarkupQuery() {
    }

    public static FileObject find(FileObject htmlFile) {
        Collection c = Lookup.getDefault().lookupAll(JavaForMarkupQueryImplementation.class);
        FileObject result = null;
        Iterator iterator = c.iterator();
        do {
            if (!iterator.hasNext()) {
                break;
            }
            JavaForMarkupQueryImplementation impl = (JavaForMarkupQueryImplementation)iterator.next();
            result = impl.find(htmlFile);
        } while (result == null);
        return result;
    }
}
