/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.refactoring;

import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.SingleCopyRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.openide.ErrorManager;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tim Boudreau
 */
@ServiceProvider(service = RefactoringPluginFactory.class)
public class WicketRefactoringFactory implements RefactoringPluginFactory {

    private static final ErrorManager ERR = ErrorManager.getDefault().getInstance("org.netbeans.modules.web.wicket.refactoring");

    @Override
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        if (refactoring instanceof RenameRefactoring) {
            ERR.log("WicketRenameRefactoringPlugin will be called");
            return new WicketRenameRefactoringPlugin((RenameRefactoring)refactoring);
        }
        if (refactoring instanceof MoveRefactoring) {
            ERR.log("WicketMoveClassRefactoringPlugin will be called");
            return new WicketMoveClassRefactoringPlugin((MoveRefactoring)refactoring);
        }
        if (refactoring instanceof SingleCopyRefactoring) {
            ERR.log("WicketCopyClassRefactoringPlugin will be called");
            return new WicketCopyClassRefactoringPlugin((SingleCopyRefactoring)refactoring);
        }
        return null;
    }
}
