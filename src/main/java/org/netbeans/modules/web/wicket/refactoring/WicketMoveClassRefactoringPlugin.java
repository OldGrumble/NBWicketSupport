/*
 * Decompiled with CFR 0_130.
 * 
 * Could not load the following classes:
 *  org.netbeans.api.project.FileOwnerQuery
 *  org.netbeans.api.project.Project
 *  org.netbeans.modules.refactoring.api.AbstractRefactoring
 *  org.netbeans.modules.refactoring.api.MoveRefactoring
 *  org.netbeans.modules.refactoring.api.Problem
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
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.web.wicket.refactoring.WicketCopyClassRefactoringPlugin;
import org.netbeans.modules.web.wicket.refactoring.WicketRenameRefactoringPlugin;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class WicketMoveClassRefactoringPlugin implements RefactoringPlugin {

    private static final ErrorManager ERR = ErrorManager.getDefault().getInstance("org.netbeans.modules.web.wicket.refactoring");
    private MoveRefactoring refactoring;

    public WicketMoveClassRefactoringPlugin(MoveRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    @Override
    public Problem preCheck() {
        return null;
    }

    @Override
    public Problem checkParameters() {
        return null;
    }

    @Override
    public Problem fastCheckParameters() {
        return null;
    }

    public void cancelRequest() {
    }

    public Problem prepare(RefactoringElementsBag bag) {
        try {
            ERR.log("Prepare moving: ");
            URL target = (URL)this.refactoring.getTarget().lookup(URL.class);
            if (target == null) {
                return new Problem(true, "no_dest");
            }
            Project project = FileOwnerQuery.getOwner((URI)target.toURI());
            if (project == null || !WicketProjectQuery.isWicket(project)) {
                return null;
            }
            if (FileUtil.getArchiveFile((URL)target) != null) {
                return new Problem(true, NbBundle.getMessage(WicketMoveClassRefactoringPlugin.class, (String)"dest_archive_file"));
            }
            if (!"file".equals(target.getProtocol())) {
                return new Problem(true, NbBundle.getMessage(WicketMoveClassRefactoringPlugin.class, (String)"bad_file", (Object)target));
            }
            FileObject dest = URLMapper.findFileObject((URL)target);
            if (dest == null) {
                return new Problem(true, NbBundle.getMessage(WicketMoveClassRefactoringPlugin.class, (String)"bad_file"));
            }
            if (!dest.isFolder()) {
                return new Problem(true, NbBundle.getMessage(WicketMoveClassRefactoringPlugin.class, (String)"not_folder"));
            }
            Collection<? extends FileObject> toMove = this.refactoring.getRefactoringSource().lookupAll(FileObject.class);
            HashSet<String> used = new HashSet<String>();
            for (FileObject javaFile : toMove) {
                FileObject markup = MarkupForJavaQuery.find(javaFile);
                if (markup == null || used.contains(markup.getPath())) {
                    continue;
                }
                FileObject check = dest.getFileObject(markup.getName(), markup.getExt());
                if (check != null) {
                    return new Problem(true, NbBundle.getMessage(WicketCopyClassRefactoringPlugin.class, (String)"msg_dest_exists", (Object)dest.getNameExt(), (Object)markup.getNameExt()));
                }
                used.add(markup.getPath());
                if (markup == null) {
                    continue;
                }
                HTMLMoveRefactoringElement element = new HTMLMoveRefactoringElement(markup, dest, markup.getParent());
                bag.addFileChange((AbstractRefactoring)this.refactoring, (RefactoringElementImplementation)element);
            }
            return null;
        } catch (URISyntaxException ex) {
            throw new AssertionError(ex);
        }
    }

    public class HTMLMoveRefactoringElement
            extends SimpleRefactoringElementImplementation {

        private FileObject foHtml;
        private FileObject oldFolder;
        private FileObject newFolder;

        HTMLMoveRefactoringElement(FileObject fo, FileObject newFolder, FileObject oldFolder) {
            this.foHtml = fo;
            this.newFolder = newFolder;
            this.oldFolder = oldFolder;
            ERR.log("HTMLMoveRefactoringElement created:");
            ERR.log("html file: " + (Object)this.foHtml);
            ERR.log("oldPath: " + (Object)oldFolder);
            ERR.log("newPath: " + (Object)newFolder);
        }

        public void performChange() {
            ERR.log("Perform moving");
            FileLock fileLock = null;
            try {
                fileLock = this.foHtml.lock();
                this.foHtml = this.foHtml.move(fileLock, this.newFolder, this.foHtml.getName(), this.foHtml.getExt());
                ERR.log("moving done");
            } catch (IOException ex) {
                ErrorManager.getDefault().notify((Throwable)ex);
            } finally {
                if (fileLock != null) {
                    fileLock.releaseLock();
                }
            }
        }

        public void undoChange() {
            ERR.log("Perform moving undo");
            FileLock fileLock = null;
            try {
                fileLock = this.foHtml.lock();
                this.foHtml = this.foHtml.move(fileLock, this.oldFolder, this.foHtml.getName(), this.foHtml.getExt());
                ERR.log("moving done");
            } catch (IOException ex) {
                ErrorManager.getDefault().notify((Throwable)ex);
            } finally {
                if (fileLock != null) {
                    fileLock.releaseLock();
                }
            }
        }

        public String getText() {
            return NbBundle.getMessage(WicketCopyClassRefactoringPlugin.class, (String)"lbl_move", (Object)this.foHtml.getNameExt(), (Object)this.newFolder.getName());
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
