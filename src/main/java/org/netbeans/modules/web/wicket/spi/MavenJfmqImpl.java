/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.spi;

import java.io.File;
import org.netbeans.spi.wicket.JavaForMarkupQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tim Boudreau
 */
@ServiceProvider(service = JavaForMarkupQueryImplementation.class, position = 20)
public class MavenJfmqImpl implements JavaForMarkupQueryImplementation {

    private StringBuilder packageName = new StringBuilder();
    private boolean found;
    private String javaFile;

    @Override
    public FileObject find(FileObject markup) {
        assert (markup != null);
        this.findJavaFolder(markup);
        if (this.javaFile != null) {
            return FileUtil.toFileObject((File)new File(this.javaFile));
        }
        return null;
    }

    private void findJavaFolder(FileObject fileObjectToIdentify) {
        this.found = false;
        if (fileObjectToIdentify != null) {
            String name = fileObjectToIdentify.getName();
            if (name.equals("resources")) {
                this.found = true;
                FileObject javaFolder = fileObjectToIdentify.getParent().getFileObject("java");
                if (javaFolder != null) {
                    this.javaFile = javaFolder.getPath() + this.packageName.toString() + ".java";
                    this.packageName = new StringBuilder();
                }
            } else if (!this.found) {
                this.packageName.insert(0, "/" + fileObjectToIdentify.getName());
                this.findJavaFolder(fileObjectToIdentify.getParent());
            }
        }
    }
}
