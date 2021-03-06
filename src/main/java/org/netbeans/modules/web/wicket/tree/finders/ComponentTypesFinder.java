/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.tree.finders;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.web.wicket.tree.util.Utils;

/**
 * 
 * @author Tim Boudreau
 */
public class ComponentTypesFinder extends TreeScanner<Void, Set<TypeMirror>> {

    private final CompilationController cc;

    ComponentTypesFinder(CompilationController cc) {
        this.cc = cc;
    }

    @Override
    public Void visitClass(ClassTree tree, Set<TypeMirror> set) {
        Element el = this.cc.getTrees().getElement(TreePath.getPath(this.cc.getCompilationUnit(), (Tree)tree));
        TypeMirror tm = el.asType();
        if (tm != null && Utils.isWebMarkupContainer(tm, this.cc.getTypes())) {
            set.add(tm);
        }
        return (Void)super.visitClass(tree, set);
    }

    @Override
    public Void visitNewClass(NewClassTree tree, Set<TypeMirror> set) {
        boolean isWmc;
        TypeMirror mirror = this.cc.getTrees().getTypeMirror(TreePath.getPath(this.cc.getCompilationUnit(), (Tree)tree));
        boolean bl = isWmc = mirror != null;
        if (mirror == null) {
            return null;
        }
        isWmc = Utils.isWebMarkupContainer(mirror, this.cc.getTypes());
        if (isWmc) {
            set.add(mirror);
        }
        return (Void)super.visitNewClass(tree, set);
    }

    @Override
    public Void visitVariable(VariableTree tree, Set<TypeMirror> set) {
        Tree type = tree.getType();
        Element e = this.cc.getTrees().getElement(TreePath.getPath(this.cc.getCompilationUnit(), type));
        TypeMirror tm = e.asType();
        if (tm != null && Utils.isWebMarkupContainer(tm, this.cc.getTypes())) {
            set.add(tm);
        }
        return (Void)super.visitVariable(tree, set);
    }
}
