/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.refactoring;

import java.io.IOException;
import java.text.MessageFormat;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.wicket.MarkupForJavaQuery;
import org.netbeans.api.wicket.WicketProjectQuery;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Tim Boudreau
 */
public class WicketRenameRefactoringPlugin implements RefactoringPlugin {

    private static final ErrorManager ERR = ErrorManager.getDefault().getInstance("org.netbeans.modules.web.wicket.refactoring");

    private final RenameRefactoring refactoring;

    public WicketRenameRefactoringPlugin(RenameRefactoring refactoring) {
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

    @Override
    public void cancelRequest() {
    }

    @Override
    public Problem prepare(RefactoringElementsBag bag) {
        DataObject dob;
        String name = this.refactoring.getNewName();
        Lookup source = this.refactoring.getRefactoringSource();
        FileObject java = (FileObject)source.lookup(FileObject.class);
        if (java == null && (dob = (DataObject)source.lookup(DataObject.class)) != null) {
            java = dob.getPrimaryFile();
        }
        if (java == null) {
            return null;
        }
        FileObject target = MarkupForJavaQuery.find(java);
        if (target == null) {
            return null;
        }
        Project project = FileOwnerQuery.getOwner((FileObject)target);
        if (project == null || !WicketProjectQuery.isWicket(project)) {
            return null;
        }
        if (target == null) {
            return new Problem(true, NbBundle.getMessage(WicketRenameRefactoringPlugin.class, (String)"no_file"));
        }
        if (name.length() == 0) {
            return new Problem(true, NbBundle.getMessage(WicketRenameRefactoringPlugin.class, (String)"empty_file_name"));
        }
        FileObject fld = target.getParent();
        FileObject existing = fld.getFileObject(name, "html");
        if (existing != null) {
            return new Problem(true, NbBundle.getMessage(WicketRenameRefactoringPlugin.class, (String)"markup_file_exists", (Object)existing.getNameExt()));
        }
        HTMLRenameRefactoringElement el = new HTMLRenameRefactoringElement(target, name, target.getName());
        bag.addFileChange((AbstractRefactoring)this.refactoring, (RefactoringElementImplementation)el);
        System.err.println("Added to bag " + (Object)((Object)el));
        return null;
    }

    private final class HTMLRenameRefactoringElement
            extends SimpleRefactoringElementImplementation {

        private final FileObject foHtml;
        private final String oldName;
        private final String newName;

        HTMLRenameRefactoringElement(FileObject fo, String newName, String oldName) {
            this.foHtml = fo;
            this.newName = newName;
            this.oldName = oldName;
        }

        @Override
        public String toString() {
            return "Rename " + this.foHtml.getPath() + " from " + this.oldName + " to " + this.newName;
        }

        @Override
        public void performChange() {
            try {
                FileLock lock = this.foHtml.lock();
                try {
                    this.foHtml.rename(lock, this.newName, this.foHtml.getExt());
                } finally {
                    lock.releaseLock();
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace((Throwable)ioe);
            }
        }

        @Override
        public void undoChange() {
            ERR.log("Perform undo");
            FileLock fileLock = null;
            try {
                fileLock = this.foHtml.lock();
                this.foHtml.rename(fileLock, this.oldName, this.foHtml.getExt());
                ERR.log("ranaming done");
            } catch (IOException ex) {
                ErrorManager.getDefault().notify((Throwable)ex);
            } finally {
                if (fileLock != null) {
                    fileLock.releaseLock();
                }
            }
        }

        @Override
        public String getText() {
            return NbBundle.getMessage(WicketCopyClassRefactoringPlugin.class, (String)"lbl_rename", (Object)this.foHtml.getNameExt(), (Object)this.newName);
        }

        @Override
        public String getDisplayText() {
            return MessageFormat.format(NbBundle.getMessage(WicketRenameRefactoringPlugin.class, (String)"TXT_WicketHtmlRename"), this.newName);
        }

        @Override
        public FileObject getParentFile() {
            return this.foHtml;
        }

        @Override
        public PositionBounds getPosition() {
            return null;
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }
    }

}
