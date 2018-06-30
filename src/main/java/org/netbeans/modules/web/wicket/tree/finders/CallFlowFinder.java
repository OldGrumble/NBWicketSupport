/*
 * Some license issues have still to be clarified, especially for the "borrowed"
 * package, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.tree.finders;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import java.util.List;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.CompilationController;

class CallFlowFinder extends TreeScanner<Void, List<MethodTree>> {

    private final CompilationController cc;
    boolean inInvocation;

    CallFlowFinder(CompilationController cc) {
        this.cc = cc;
    }

    @Override
    public Void visitMethod(MethodTree tree, List<MethodTree> l) {
        if (inInvocation) {
            System.err.println("Add invocation of " + tree.getName());
            l.add(tree);
        }
        return (Void)super.visitMethod(tree, l);
    }

    @Override
    public Void visitMethodInvocation(MethodInvocationTree tree, List<MethodTree> list) {
        inInvocation = true;
        try {
            ExpressionTree select = tree.getMethodSelect();
            Element element;
            try {
                element = cc.getTrees().getElement(TreePath.getPath(cc.getCompilationUnit(), (Tree)select));
            } catch (NullPointerException ex) {
                Void void_ = (Void)super.visitMethodInvocation(tree, list);
                inInvocation = false;
                return void_;
            }
            Tree methodTree = cc.getTrees().getTree(element);
            if (methodTree instanceof MethodTree && !list.contains((MethodTree)methodTree)) {
                methodTree.accept(this, list);
            }
        } finally {
            inInvocation = false;
        }
        return (Void)super.visitMethodInvocation(tree, list);
    }
}
