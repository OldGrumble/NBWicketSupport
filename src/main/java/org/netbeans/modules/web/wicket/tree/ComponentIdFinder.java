/*
 * Decompiled with CFR 0_130.
 * 
 * Could not load the following classes:
 *  org.netbeans.api.java.source.CompilationController
 */
package org.netbeans.modules.web.wicket.tree;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.web.wicket.tree.ConstructorFinder;

public class ComponentIdFinder extends TreeScanner<Void, List<String>> {

    private CompilationController cc;
    private boolean inArg;
    private boolean searchingConstructor;

    ComponentIdFinder(CompilationController cc) {
        this.cc = cc;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void scanInArgs(List<? extends ExpressionTree> args, List<String> s) {
        for (ExpressionTree t : args) {
            TypeMirror argType = this.cc.getTrees().getTypeMirror(TreePath.getPath(this.cc.getCompilationUnit(), (Tree)t));
            if (argType == null || !"java.lang.String".equals(argType.toString())) {
                continue;
            }
            this.inArg = true;
            try {
                t.accept(this, s);
            } finally {
                this.inArg = false;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Void visitNewClass(NewClassTree tree, List<String> s) {
        List<? extends ExpressionTree> args = tree.getArguments();
        this.scanInArgs(args, s);
        if (args.isEmpty()) {
            TypeMirror classType = this.cc.getTrees().getTypeMirror(TreePath.getPath(this.cc.getCompilationUnit(), (Tree)tree));
            TypeElement el = (TypeElement)this.cc.getTypes().asElement(classType);
            System.err.println("Super element is " + el.getQualifiedName());
            if (el != null) {
                ClassTree cTree = this.cc.getTrees().getTree(el);
                HashSet constructors = new HashSet();
                cTree.accept(new ConstructorFinder(), constructors);
                this.searchingConstructor = true;
                try {
                    MethodTree constructor;
                    for (Iterator iterator = constructors.iterator(); iterator.hasNext(); constructor.accept(this, s)) {
                        constructor = (MethodTree)iterator.next();
                    }
                } finally {
                    this.searchingConstructor = false;
                }
            }
        }
        return (Void)super.visitNewClass(tree, s);
    }

    @Override
    public Void visitMethodInvocation(MethodInvocationTree tree, List<String> names) {
        boolean isSuper;
        if (this.searchingConstructor && (isSuper = this.isSuperCallInConstructor(tree))) {
            List<? extends ExpressionTree> l = tree.getArguments();
            this.scanInArgs(l, names);
        }
        return (Void)super.visitMethodInvocation(tree, names);
    }

    private boolean isSuperCallInConstructor(MethodInvocationTree tree) {
        return tree.toString().startsWith("super");
    }

    @Override
    public Void visitVariable(VariableTree arg0, List<String> arg1) {
        return (Void)super.visitVariable(arg0, arg1);
    }

    @Override
    public Void visitIdentifier(IdentifierTree tree, List<String> names) {
        TypeMirror type;
        if (this.inArg && "java.lang.String".equals((type = this.cc.getTrees().getTypeMirror(TreePath.getPath(this.cc.getCompilationUnit(), (Tree)tree))).toString())) {
            Element e = this.cc.getTrees().getElement(TreePath.getPath(this.cc.getCompilationUnit(), (Tree)tree));
            Tree t = this.cc.getTrees().getTree(e);
            if (t instanceof VariableTree) {
                VariableTree vt = (VariableTree)t;
                vt.accept(this, names);
            }
        }
        return (Void)super.visitIdentifier(tree, names);
    }

    @Override
    public Void visitLiteral(LiteralTree tree, List<String> s) {
        if (this.inArg) {
            s.add(tree.toString());
        }
        return (Void)super.visitLiteral(tree, s);
    }
}
