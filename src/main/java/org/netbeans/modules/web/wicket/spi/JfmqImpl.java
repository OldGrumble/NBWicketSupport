/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.spi;

import org.netbeans.spi.wicket.JavaForMarkupQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tim Boudreau
 */
@ServiceProvider(service = JavaForMarkupQueryImplementation.class, position = 10)
public class JfmqImpl implements JavaForMarkupQueryImplementation {

    @Override
    public FileObject find(FileObject markup) {
        assert (markup != null);
        FileObject parent = markup.getParent();
        FileObject result = parent.getFileObject(markup.getName(), "java");
        return result;
    }
}
