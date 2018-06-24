/*
 * Decompiled with CFR 0_130.
 * 
 * Could not load the following classes:
 *  org.netbeans.api.java.source.CompilationController
 */
package org.netbeans.modules.web.wicket.tree;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import java.util.Set;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.web.wicket.tree.Utils;

public class ComponentConstructorInvocationFinder
extends TreeScanner<Void, Set<NewClassTree>> {
    private final CompilationController cc;

    ComponentConstructorInvocationFinder(CompilationController cc) {
        this.cc = cc;
    }

    @Override
    public Void visitNewClass(NewClassTree t, Set<NewClassTree> coll) {
        boolean isWmc;
        TypeMirror mirror = this.cc.getTrees().getTypeMirror(TreePath.getPath(this.cc.getCompilationUnit(), (Tree)t));
        boolean bl = isWmc = mirror != null;
        if (mirror == null) {
            return null;
        }
        isWmc = Utils.isWebMarkupContainer(mirror, this.cc.getTypes());
        if (isWmc) {
            coll.add(t);
        }
        return (Void)super.visitNewClass(t, coll);
    }
}

