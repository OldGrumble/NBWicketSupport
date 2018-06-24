/*
 * Decompiled with CFR 0_130.
 * 
 * Could not load the following classes:
 *  org.netbeans.api.java.source.CompilationController
 */
package org.netbeans.modules.web.wicket.tree;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import java.io.PrintStream;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import org.netbeans.api.java.source.CompilationController;

class CallFlowFinder
extends TreeScanner<Void, List<MethodTree>> {
    private final CompilationController cc;
    boolean inInvocation;

    CallFlowFinder(CompilationController cc) {
        this.cc = cc;
    }

    @Override
    public Void visitMethod(MethodTree tree, List<MethodTree> l) {
        if (this.inInvocation) {
            System.err.println("Add invocation of " + tree.getName());
            l.add(tree);
        }
        return (Void)super.visitMethod(tree, l);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Void visitMethodInvocation(MethodInvocationTree tree, List<MethodTree> l) {
        this.inInvocation = true;
        try {
            ExpressionTree sel = tree.getMethodSelect();
            Element e = null;
            try {
                e = this.cc.getTrees().getElement(TreePath.getPath(this.cc.getCompilationUnit(), (Tree)sel));
            }
            catch (NullPointerException ex) {
                Void void_ = (Void)super.visitMethodInvocation(tree, l);
                this.inInvocation = false;
                return void_;
            }
            Tree methodTree = this.cc.getTrees().getTree(e);
            if (!l.contains(methodTree)) {
                methodTree.accept(this, l);
            }
        }
        finally {
            this.inInvocation = false;
        }
        return (Void)super.visitMethodInvocation(tree, l);
    }
}

