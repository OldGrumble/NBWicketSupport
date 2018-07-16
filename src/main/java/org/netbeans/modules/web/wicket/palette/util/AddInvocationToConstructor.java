/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.palette.util;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
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
public class AddInvocationToConstructor extends TreePathScanner<Void, Void> {

    private static class WorkerTask implements Task<WorkingCopy> {

        private final MethodTree constructor;
        private final String code;

        public WorkerTask(MethodTree constructor, String code) {
            this.constructor = constructor;
            this.code = code;
        }

        @Override
        public void run(WorkingCopy workingCopy) throws IOException {
            workingCopy.toPhase(JavaSource.Phase.PARSED);
            workingCopy.rewrite(constructor.getBody(), workingCopy.getTreeMaker().addBlockStatement(constructor.getBody(), getExpressionStatement(workingCopy, code)));
        }

        private ExpressionStatementTree getExpressionStatement(WorkingCopy workingCopy, String code) {
            TreeMaker make = workingCopy.getTreeMaker();
            ExpressionStatementTree est = make.ExpressionStatement((ExpressionTree)make.MethodInvocation(Collections.emptyList(), (ExpressionTree)make.Identifier((CharSequence)"add"), Collections.singletonList(make.Identifier((CharSequence)code))));
            return est;
        }
    }

    private final CompilationInfo info;
    private final JavaSource source;
    private final String code;

    public AddInvocationToConstructor(JavaSource source, CompilationInfo info, String code) {
        this.info = info;
        this.source = source;
        this.code = code;
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

                    Task<WorkingCopy> task1 = new WorkerTask(constructor, code);
                    try {
                        ModificationResult result = this.source.runModificationTask(task1);
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
