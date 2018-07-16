/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.palette.util;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePathScanner;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.openide.util.Exceptions;

/**
 *
 * @author Tim Boudreau
 */
public class AddVariableToConstructor extends TreePathScanner<Void, Void> {

    private static class WorkerTask implements Task<WorkingCopy> {

        private final MethodTree constructor;

        public WorkerTask(final MethodTree constructor) {
            this.constructor = constructor;
        }

        @Override
        public void run(WorkingCopy workingCopy) throws IOException {
            workingCopy.toPhase(JavaSource.Phase.PARSED);
            workingCopy.rewrite(constructor.getBody(), workingCopy.getTreeMaker().addBlockStatement(constructor.getBody(), getVariable(workingCopy)));
        }

        private VariableTree getVariable(WorkingCopy workingCopy) {
            TreeMaker make = workingCopy.getTreeMaker();
            TypeElement listClass = workingCopy.getElements().getTypeElement("java.util.List");
            ExpressionTree importListClass = make.QualIdent(listClass);
            TypeElement arrayListClass = workingCopy.getElements().getTypeElement("java.util.ArrayList");
            ExpressionTree importArrayListClass = make.QualIdent(arrayListClass);
            NewClassTree mapConstructor = make.NewClass(null, Collections.emptyList(), importArrayListClass, Collections.emptyList(), null);
            VariableTree vt = make.Variable(make.Modifiers(Collections.emptySet(), Collections.emptyList()), "NAMES", importListClass, mapConstructor);
            return vt;
        }
    }

    private final CompilationInfo info;
    private final JavaSource source;

    public AddVariableToConstructor(JavaSource source, CompilationInfo info) {
        this.info = info;
        this.source = source;
    }

    @Override
    public Void visitClass(ClassTree classTree, Void voidObject) {
        Element el = this.info.getTrees().getElement(this.getCurrentPath());
        if (el != null) {
            TypeElement te = (TypeElement)el;
            List<? extends Element> enclosedElements = te.getEnclosedElements();
            for (Element enclosedElement : enclosedElements) {
                if (enclosedElement.getKind() == ElementKind.CONSTRUCTOR) {
                    ExecutableElement exe = (ExecutableElement)enclosedElement;

                    final MethodTree constructor = info.getTrees().getTree(exe);

                    Task<WorkingCopy> task1 = new WorkerTask(constructor);
                    try {
                        ModificationResult result = source.runModificationTask(task1);
                        result.commit();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace((Throwable)ex);
                    }
                }
            }
        }
        return null;
    }
}
