/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.tree.finders;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import java.util.Set;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.CompilationController;

/**
 *
 * @author Tim Boudreau
 */
final class AssignmentTracer extends TreeScanner<Void, Set<NewClassTree>> {

    private final CompilationController cc;
    private final Tree scan;
    private final Element identifier;
    private AssignmentTracer parent;
    private int tryFindReturnType = 0;

    AssignmentTracer(CompilationController cc, Tree scan, Element identifier) {
        this.cc = cc;
        this.scan = scan;
        this.identifier = identifier;
    }

    AssignmentTracer(CompilationController cc, Tree scan, Element identifier, AssignmentTracer parent) {
        this.cc = cc;
        this.scan = scan;
        this.parent = parent;
        this.identifier = identifier;
    }

    boolean isKnown(Element el) {
        return el.equals(identifier) || parent != null && parent.isKnown(el);
    }

    @Override
    public Void visitReturn(ReturnTree tree, Set<NewClassTree> set) {
        if (tryFindReturnType > 0) {
            ExpressionTree ret = tree.getExpression();
            switch (ret.getKind()) {
                case NEW_CLASS: {
                    set.add((NewClassTree)tree.getExpression());
                    break;
                }
                case IDENTIFIER: {
                    Element identified = cc.getTrees().getElement(TreePath.getPath(cc.getCompilationUnit(), (Tree)ret));
                    if (isKnown(identified)) {
                        break;
                    }
                    Tree newTree = cc.getTrees().getTree(identified);
                    AssignmentTracer at = new AssignmentTracer(cc, scan, identified);
                    newTree.accept(at, set);
                    break;
                }
                case METHOD_INVOCATION: {
                    MethodInvocationTree inv = (MethodInvocationTree)ret;
                    traceMethodInvocation(inv, set);
                    break;
                }
                default: {
                    System.err.println("Could not analyze return expression " + ret + " of kind " + (Object)((Object)ret.getKind()));
                }
            }
        }
        return super.visitReturn(tree, set);
    }

    private void traceMethodInvocation(MethodInvocationTree mit, Set<NewClassTree> set) {
        tryFindReturnType++;
        try {
            ExpressionTree select = mit.getMethodSelect();
            Element el = cc.getTrees().getElement(TreePath.getPath(cc.getCompilationUnit(), (Tree)select));
            Tree methodTree = cc.getTrees().getTree(el);
            methodTree.accept(this, set);
        } catch (StackOverflowError ex) {
            System.err.println("Recursion or cicular call encountered from " + mit);
        } finally {
            tryFindReturnType--;
        }
    }

    @Override
    public Void visitVariable(VariableTree tree, Set<NewClassTree> set) {
        if (!identifier.getSimpleName().equals(tree.getName())) {
            return null;
        }
        ExpressionTree ect = tree.getInitializer();
        if (ect != null) {
            switch (ect.getKind()) {
                case METHOD_INVOCATION: {
                    traceMethodInvocation((MethodInvocationTree)ect, set);
                    break;
                }
                case NEW_CLASS: {
                    set.add((NewClassTree)ect);
                    break;
                }
                case IDENTIFIER: {
//                    IdentifierTree id = (IdentifierTree)ect;
                    Element nue = cc.getTrees().getElement(TreePath.getPath(cc.getCompilationUnit(), ect));
                    if (this.isKnown(nue)) {
                        break;
                    }
                    Tree newTree = cc.getTrees().getTree(nue);
                    AssignmentTracer newTracer = new AssignmentTracer(cc, scan, nue, this);
                    newTree.accept(newTracer, set);
                }
            }
        }
        return super.visitVariable(tree, set);
    }

    @Override
    public Void visitAssignment(AssignmentTree tree, Set<NewClassTree> set) {
        Element varEl;
        ExpressionTree var = tree.getVariable();
        try {
            varEl = cc.getTrees().getElement(TreePath.getPath(cc.getCompilationUnit(), (Tree)var));
        } catch (NullPointerException npe) {
            return null;
        }
        if (identifier.equals(varEl)) {
            ExpressionTree to = tree.getExpression();
            if (to instanceof NewClassTree) {
                set.add((NewClassTree)to);
            } else if (to instanceof IdentifierTree) {
                try {
                    Element nue = cc.getTrees().getElement(TreePath.getPath(cc.getCompilationUnit(), (Tree)to));
                    if (!this.isKnown(nue)) {
                        Tree newTree = cc.getTrees().getTree(nue);
                        AssignmentTracer newTracer = new AssignmentTracer(cc, scan, identifier, this);
                        newTree.accept(newTracer, set);
                    }
                } catch (NullPointerException e) {
                    return null;
                }
            }
        }
        return super.visitAssignment(tree, set);
    }
}
