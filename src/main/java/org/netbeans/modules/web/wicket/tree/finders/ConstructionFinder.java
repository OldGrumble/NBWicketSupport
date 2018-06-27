/*
 * Some license issues have still to be clarified, especially for the "borrowed"
 * package, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.tree.finders;

import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import java.util.Set;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationController;

public class ConstructionFinder extends TreeScanner<Void, Set<NewClassTree>> {

    private final CompilationController cc;
    private final TypeMirror type;

    ConstructionFinder(CompilationController cc, TypeMirror type) {
        this.type = type;
        this.cc = cc;
    }

    @Override
    public Void visitNewClass(NewClassTree tree, Set<NewClassTree> set) {
        TypeMirror mirror = cc.getTrees().getTypeMirror(TreePath.getPath(cc.getCompilationUnit(), (Tree)tree));
        if (mirror.equals(type)) {
            set.add(tree);
        }
        return (Void)super.visitNewClass(tree, set);
    }
}
