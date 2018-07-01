/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.tree.finders;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationController;

/**
 * 
 * @author Tim Boudreau
 */
public class ComponentIdFinder extends TreeScanner<Void, List<String>> {

    private final CompilationController cc;
    private boolean inArg;
    private boolean searchingConstructor;

    public ComponentIdFinder(CompilationController cc) {
        this.cc = cc;
    }

    private void scanInArgs(List<? extends ExpressionTree> args, List<String> s) {
        for (ExpressionTree t : args) {
            TypeMirror argType = cc.getTrees().getTypeMirror(TreePath.getPath(cc.getCompilationUnit(), (Tree)t));
            if (argType != null && "java.lang.String".equals(argType.toString())) {
                inArg = true;
                try {
                    t.accept(this, s);
                } finally {
                    inArg = false;
                }
            }
        }
    }

    @Override
    public Void visitNewClass(NewClassTree tree, List<String> s) {
        List<? extends ExpressionTree> args = tree.getArguments();
        scanInArgs(args, s);
        if (args.isEmpty()) {
            TypeMirror classType = cc.getTrees().getTypeMirror(TreePath.getPath(cc.getCompilationUnit(), (Tree)tree));
            TypeElement el = (TypeElement)cc.getTypes().asElement(classType);
            if (el != null) {
                System.err.println("Super element is " + el.getQualifiedName());
                ClassTree cTree = cc.getTrees().getTree(el);
                Set constructors = new HashSet();
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
            } else {
                System.err.println("Super element is null");
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
        if (this.inArg && "java.lang.String".equals((type = cc.getTrees().getTypeMirror(TreePath.getPath(cc.getCompilationUnit(), (Tree)tree))).toString())) {
            Element e = cc.getTrees().getElement(TreePath.getPath(cc.getCompilationUnit(), (Tree)tree));
            Tree t = cc.getTrees().getTree(e);
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
