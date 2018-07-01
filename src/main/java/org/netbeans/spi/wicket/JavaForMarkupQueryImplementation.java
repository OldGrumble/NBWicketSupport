/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.spi.wicket;

/**
 * 
 * @author Tim Boudreau
 */
import org.openide.filesystems.FileObject;

public interface JavaForMarkupQueryImplementation {

    public abstract FileObject find(FileObject fileobject);
}
