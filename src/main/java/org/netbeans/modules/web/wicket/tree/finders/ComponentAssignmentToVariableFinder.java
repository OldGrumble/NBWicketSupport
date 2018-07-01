/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.tree.finders;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import java.util.Set;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.web.wicket.tree.util.Utils;

/**
 * 
 * @author Tim Boudreau
 */
public class ComponentAssignmentToVariableFinder extends TreeScanner<Void, Set<Tree>> {

    private final CompilationController cc;

    public ComponentAssignmentToVariableFinder(CompilationController cc) {
        this.cc = cc;
    }

    @Override
    public Void visitVariable(VariableTree tree, Set<Tree> set) {
        Tree typeTree = tree.getType();
        boolean isWebMarkupContainer = Utils.isWebMarkupContainer(this.cc.getTrees(), this.cc.getTypes(), this.cc.getCompilationUnit(), typeTree);
        if (isWebMarkupContainer) {
            set.add(tree);
        }
        return (Void)super.visitVariable(tree, set);
    }

    @Override
    public Void visitAssignment(AssignmentTree tree, Set<Tree> set) {
        ExpressionTree var = tree.getVariable();
        System.err.println("Assignment of " + var + ": " + tree);
        boolean isWebMarkupContainer = Utils.isWebMarkupContainer(this.cc.getTrees(), this.cc.getTypes(), this.cc.getCompilationUnit(), var) || Utils.isWebMarkupContainer(this.cc.getTrees(), this.cc.getTypes(), this.cc.getCompilationUnit(), tree.getExpression());
        System.err.println("Assignment of " + var + " markup container? " + isWebMarkupContainer);
        if (isWebMarkupContainer) {
            set.add(tree);
        }
        return (Void)super.visitAssignment(tree, set);
    }
}
