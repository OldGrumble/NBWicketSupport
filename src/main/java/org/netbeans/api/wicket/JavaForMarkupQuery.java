/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.api.wicket;

import java.util.Collection;
import java.util.Iterator;
import org.netbeans.spi.wicket.JavaForMarkupQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Tim Boudreau
 */
public final class JavaForMarkupQuery {

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

    private JavaForMarkupQuery() {
    }
}
