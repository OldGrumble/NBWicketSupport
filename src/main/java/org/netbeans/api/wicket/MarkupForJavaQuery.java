/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.api.wicket;

import java.util.Collection;
import java.util.Iterator;
import org.netbeans.spi.wicket.MarkupForJavaQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Tim Boudreau
 */
public class MarkupForJavaQuery {

    public static FileObject find(FileObject javaFile) {
        Collection c = Lookup.getDefault().lookupAll(MarkupForJavaQueryImplementation.class);
        FileObject result = null;
        Iterator iterator = c.iterator();
        do {
            if (!iterator.hasNext()) {
                break;
            }
            MarkupForJavaQueryImplementation impl = (MarkupForJavaQueryImplementation)iterator.next();
            result = impl.find(javaFile);
        } while (result == null);
        return result;
    }
}
