/*
 * Decompiled with CFR 0_130.
 * 
 * Could not load the following classes:
 *  org.netbeans.api.project.FileOwnerQuery
 *  org.netbeans.api.project.Project
 *  org.netbeans.modules.refactoring.api.AbstractRefactoring
 *  org.netbeans.modules.refactoring.api.MultipleCopyRefactoring
 *  org.netbeans.modules.refactoring.api.Problem
 *  org.netbeans.modules.refactoring.api.SingleCopyRefactoring
 *  org.netbeans.modules.refactoring.spi.RefactoringElementImplementation
 *  org.netbeans.modules.refactoring.spi.RefactoringElementsBag
 *  org.netbeans.modules.refactoring.spi.RefactoringPlugin
 *  org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation
 *  org.openide.ErrorManager
 *  org.openide.filesystems.FileLock
 *  org.openide.filesystems.FileObject
 *  org.openide.filesystems.FileUtil
 *  org.openide.filesystems.URLMapper
 *  org.openide.text.PositionBounds
 *  org.openide.util.Exceptions
 *  org.openide.util.Lookup
 *  org.openide.util.NbBundle
 */
package org.netbeans.modules.web.wicket.refactoring;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.wicket.MarkupForJavaQuery;
import org.netbeans.api.wicket.WicketProjectQuery;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MultipleCopyRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.SingleCopyRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.web.wicket.refactoring.WicketRenameRefactoringPlugin;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class WicketCopyClassRefactoringPlugin implements RefactoringPlugin {

    private static final ErrorManager ERR = ErrorManager.getDefault().getInstance("org.netbeans.modules.web.wicket.refactoring");
    private AbstractRefactoring refactoring;

    public WicketCopyClassRefactoringPlugin(SingleCopyRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    public WicketCopyClassRefactoringPlugin(MultipleCopyRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    public Problem preCheck() {
        return null;
    }

    public Problem checkParameters() {
        return null;
    }

    public Problem fastCheckParameters() {
        return null;
    }

    public void cancelRequest() {
    }

    private Lookup getTarget() {
        return this.refactoring instanceof SingleCopyRefactoring ? ((SingleCopyRefactoring)this.refactoring).getTarget() : ((MultipleCopyRefactoring)this.refactoring).getTarget();
    }

    private String getNewName() {
        return this.refactoring instanceof SingleCopyRefactoring ? ((SingleCopyRefactoring)this.refactoring).getNewName() : null;
    }

    @Override
    public Problem prepare(RefactoringElementsBag bag) {
        try {
            ERR.log("Prepare moving: ");
            URL target = (URL)this.getTarget().lookup(URL.class);
            if (target == null) {
                return new Problem(true, "no_dest");
            }
            Project project = FileOwnerQuery.getOwner((URI)target.toURI());
            if (project == null || !WicketProjectQuery.isWicket(project)) {
                return null;
            }
            if (FileUtil.getArchiveFile((URL)target) != null) {
                return new Problem(true, NbBundle.getMessage(WicketCopyClassRefactoringPlugin.class, (String)"dest_archive_file"));
            }
            if (!"file".equals(target.getProtocol())) {
                return new Problem(true, NbBundle.getMessage(WicketCopyClassRefactoringPlugin.class, (String)"bad_file", (Object)target));
            }
            FileObject dest = URLMapper.findFileObject((URL)target);
            if (dest == null) {
                return new Problem(true, NbBundle.getMessage(WicketCopyClassRefactoringPlugin.class, (String)"bad_file"));
            }
            if (!dest.isFolder()) {
                return new Problem(true, NbBundle.getMessage(WicketCopyClassRefactoringPlugin.class, (String)"not_folder"));
            }
            Collection<? extends FileObject> toCopy = this.refactoring.getRefactoringSource().lookupAll(FileObject.class);
            HashSet<String> used = new HashSet<String>();
            String name = this.getNewName();
            for (FileObject javaFile : toCopy) {
                FileObject markup = MarkupForJavaQuery.find(javaFile);
                if (markup == null || used.contains(markup.getPath())) {
                    continue;
                }
                FileObject destFile = dest.getFileObject(this.getNewName(), markup.getExt());
                if (destFile != null) {
                    return new Problem(true, NbBundle.getMessage(WicketCopyClassRefactoringPlugin.class, (String)"msg_dest_exists", (Object)dest.getNameExt(), (Object)markup.getNameExt()));
                }
                used.add(markup.getPath());
                if (markup == null) {
                    continue;
                }
                HTMLCopyRefactoringElement element = new HTMLCopyRefactoringElement(markup, dest, markup.getParent(), name);
                bag.addFileChange(this.refactoring, (RefactoringElementImplementation)element);
            }
            return null;
        } catch (URISyntaxException ex) {
            throw new AssertionError(ex);
        }
    }

    private final class HTMLCopyRefactoringElement
            extends SimpleRefactoringElementImplementation {

        private FileObject foHtml;
        private final FileObject oldFolder;
        private final FileObject newFolder;
        private final String newName;

        HTMLCopyRefactoringElement(FileObject fo, FileObject newFolder, FileObject oldFolder, String newName) {
            this.foHtml = fo;
            this.newFolder = newFolder;
            this.oldFolder = oldFolder;
            this.newName = newName;
            ERR.log("HTMLMoveRefactoringElement created:");
            ERR.log("html file: " + (Object)this.foHtml);
            ERR.log("oldPath: " + oldFolder.getPath());
            ERR.log("newPath: " + newFolder.getPath());
            ERR.log("newName: " + newName);
        }

        private String getNewName() {
            return this.newName == null ? this.foHtml.getName() : this.newName;
        }

        public void performChange() {
            ERR.log("Perform copying");
            try {
                String name = this.getNewName();
                this.foHtml = this.foHtml.copy(this.newFolder, this.newName, this.foHtml.getExt());
                ERR.log("copying done");
            } catch (IOException ex) {
                Exceptions.printStackTrace((Throwable)ex);
            }
        }

        public void undoChange() {
            ERR.log("Perform moving undo");
            FileLock fileLock = null;
            try {
                fileLock = this.foHtml.lock();
                this.foHtml.delete(fileLock);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify((Throwable)ex);
            } finally {
                if (fileLock != null) {
                    fileLock.releaseLock();
                }
            }
        }

        public String getText() {
            if (this.oldFolder.equals((Object)this.newFolder)) {
                return NbBundle.getMessage(WicketCopyClassRefactoringPlugin.class, (String)"lbl_copy_simple", (Object)this.foHtml.getNameExt(), (Object)this.newFolder.getName());
            }
            return NbBundle.getMessage(WicketCopyClassRefactoringPlugin.class, (String)"lbl_copy", (Object)this.foHtml.getNameExt(), (Object)this.getNewName(), (Object)this.newFolder.getName());
        }

        public String getDisplayText() {
            return MessageFormat.format(NbBundle.getMessage(WicketRenameRefactoringPlugin.class, (String)"TXT_WicketHtmlMove"), this.foHtml.getName() + "." + this.foHtml.getExt(), this.newFolder.getPath());
        }

        public FileObject getParentFile() {
            return this.foHtml;
        }

        public PositionBounds getPosition() {
            return null;
        }

        public Lookup getLookup() {
            return Lookup.EMPTY;
        }
    }

}
