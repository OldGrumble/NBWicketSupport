/*
 * Decompiled with CFR 0_130.
 * 
 * Could not load the following classes:
 *  org.netbeans.modules.refactoring.api.AbstractRefactoring
 *  org.netbeans.modules.refactoring.api.MoveRefactoring
 *  org.netbeans.modules.refactoring.api.RenameRefactoring
 *  org.netbeans.modules.refactoring.api.SingleCopyRefactoring
 *  org.netbeans.modules.refactoring.spi.RefactoringPlugin
 *  org.netbeans.modules.refactoring.spi.RefactoringPluginFactory
 *  org.openide.ErrorManager
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
