/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.spi;

import java.io.File;
import org.netbeans.spi.wicket.MarkupForJavaQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tim Boudreau
 */
@ServiceProvider(service = MarkupForJavaQueryImplementation.class, position = 20)
public class MavenMfjqImpl implements MarkupForJavaQueryImplementation {

    private StringBuilder packageName = new StringBuilder();
    private boolean found;
    private String resource;

    @Override
    public FileObject find(FileObject javaFile) {
        assert (javaFile != null);
        this.findResourceFolder(javaFile);
        if (this.resource != null) {
            return FileUtil.toFileObject((File)new File(this.resource));
        }
        return null;
    }

    private void findResourceFolder(FileObject fileObjectToIdentify) {
        this.found = false;
        if (fileObjectToIdentify != null) {
            String name = fileObjectToIdentify.getName();
            if (name.equals("java")) {
                this.found = true;
                FileObject resourceFolder = fileObjectToIdentify.getParent().getFileObject("resources");
                if (resourceFolder != null) {
                    this.resource = resourceFolder.getPath() + this.packageName.toString() + ".html";
                    this.packageName = new StringBuilder();
                }
            } else if (!this.found) {
                this.packageName.insert(0, "/" + fileObjectToIdentify.getName());
                this.findResourceFolder(fileObjectToIdentify.getParent());
            }
        }
    }
}
