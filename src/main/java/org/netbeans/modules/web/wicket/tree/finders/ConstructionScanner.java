/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.tree.finders;

import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import java.util.Set;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.web.wicket.tree.results.TreeScanResultHandler;

/**
 * 
 * @author Tim Boudreau
 */
public class ConstructionScanner extends TreeScanner<Void, TreeScanResultHandler<NewClassTree>> {

    private final CompilationController cc;
    private final TypeMirror type;

    ConstructionScanner(CompilationController cc, TypeMirror type) {
        this.type = type;
        this.cc = cc;
    }

    @Override
    public Void visitNewClass(NewClassTree tree, TreeScanResultHandler<NewClassTree> resultHandler) {
        TypeMirror mirror = cc.getTrees().getTypeMirror(TreePath.getPath(cc.getCompilationUnit(), (Tree)tree));
        if (mirror.equals(type)) {
            resultHandler.handleResult(tree);
        }
        return (Void)super.visitNewClass(tree, resultHandler);
    }
}
