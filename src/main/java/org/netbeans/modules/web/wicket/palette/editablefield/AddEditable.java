/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.palette.editablefield;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
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
public class AddEditable extends TreePathScanner<Void, Void> {

    private final CompilationInfo info;
    private final JavaSource source;
    private final String wicketId;
    private final String initValue;

    public AddEditable(JavaSource source, CompilationInfo info, String wicketId, String initValue) {
        this.info = info;
        this.source = source;
        this.wicketId = wicketId;
        this.initValue = initValue;
    }

    @Override
    public Void visitClass(ClassTree t, Void v) {
        Element el = this.info.getTrees().getElement(this.getCurrentPath());
        if (el != null) {
            TypeElement te = (TypeElement)el;
            List<? extends Element> enclosedElements = te.getEnclosedElements();
            for (int i = 0; i < enclosedElements.size(); ++i) {
                Element enclosedElement = enclosedElements.get(i);
                if (enclosedElement.getKind() != ElementKind.CONSTRUCTOR) {
                    continue;
                }
                ExecutableElement exe = (ExecutableElement)enclosedElement;
                final MethodTree constructor = this.info.getTrees().getTree(exe);
                Task<WorkingCopy> task1 = new Task<WorkingCopy>() {

                    @Override
                    public void run(WorkingCopy workingCopy) throws IOException {
                        workingCopy.toPhase(JavaSource.Phase.PARSED);
                        workingCopy.rewrite((Tree)constructor.getBody(), workingCopy.getTreeMaker().addBlockStatement(constructor.getBody(), AddEditable.this.getVariableTree2(workingCopy)));
                    }
                };
                try {
                    ModificationResult result = this.source.runModificationTask(task1);
                    result.commit();
                } catch (IOException ex) {
                    Exceptions.printStackTrace((Throwable)ex);
                }
            }
        }
        return null;
    }

    private ExpressionStatementTree getVariableTree2(WorkingCopy workingCopy) {
        TreeMaker make = workingCopy.getTreeMaker();
        ExpressionStatementTree est = make.ExpressionStatement((ExpressionTree)make.MethodInvocation(Collections.emptyList(), (ExpressionTree)make.Identifier((CharSequence)"add"), Collections.singletonList(make.Identifier((CharSequence)("new AjaxEditableLabel(\"" + this.wicketId + "\")")))));
        return est;
    }

}
