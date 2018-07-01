/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.spi;

import org.netbeans.spi.wicket.MarkupForJavaQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tim Boudreau
 */
@ServiceProvider(service = MarkupForJavaQueryImplementation.class, position = 10)
public class MfjqImpl implements MarkupForJavaQueryImplementation {

    @Override
    public FileObject find(FileObject source) {
        assert (source != null);
        FileObject parent = source.getParent();
        FileObject result = parent.getFileObject(source.getName(), "html");
        return result;
    }
}
