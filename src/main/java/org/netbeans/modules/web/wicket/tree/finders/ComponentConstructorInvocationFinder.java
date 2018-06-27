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
import org.netbeans.modules.web.wicket.tree.util.Utils;

/**
 * A TreeScanner implementation for collecting all constructor invocationss.
 *
 * @author Geertjan Wielenga
 */
public class ComponentConstructorInvocationFinder extends TreeScanner<Void, Set<NewClassTree>> {

    /**
     * The CompilationController gets information about Java sources from the
     * compiler.
     */
    private final CompilationController cc;

    public ComponentConstructorInvocationFinder(CompilationController cc) {
        this.cc = cc;
    }

    @Override
    public Void visitNewClass(NewClassTree t, Set<NewClassTree> coll) {
        // cc.getTrees()
        TypeMirror mirror = cc.getTrees().getTypeMirror(TreePath.getPath(cc.getCompilationUnit(), (Tree)t));
        if (mirror != null) {
            boolean isWMC = Utils.isWebMarkupContainer(mirror, cc.getTypes());
            if (isWMC) {
                coll.add(t);
            }
            return (Void)super.visitNewClass(t, coll);
        } else {
            return null;
        }
    }
}
