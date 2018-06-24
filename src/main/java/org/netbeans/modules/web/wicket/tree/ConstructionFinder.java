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
import org.netbeans.api.java.source.CompilationController;

public class ConstructionFinder
extends TreeScanner<Void, Set<NewClassTree>> {
    private CompilationController cc;
    private TypeMirror type;

    ConstructionFinder(CompilationController cc, TypeMirror type) {
        this.type = type;
        this.cc = cc;
    }

    @Override
    public Void visitNewClass(NewClassTree tree, Set<NewClassTree> set) {
        TypeMirror mirror = this.cc.getTrees().getTypeMirror(TreePath.getPath(this.cc.getCompilationUnit(), (Tree)tree));
        if (mirror.equals(this.type)) {
            set.add(tree);
        }
        return (Void)super.visitNewClass(tree, set);
    }
}

