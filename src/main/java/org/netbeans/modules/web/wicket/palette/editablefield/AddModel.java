/*
 * Decompiled with CFR 0_130.
 * 
 * Could not load the following classes:
 *  org.netbeans.api.java.source.CompilationInfo
 *  org.netbeans.api.java.source.JavaSource
 *  org.netbeans.api.java.source.JavaSource$Phase
 *  org.netbeans.api.java.source.ModificationResult
 *  org.netbeans.api.java.source.Task
 *  org.netbeans.api.java.source.TreeMaker
 *  org.netbeans.api.java.source.WorkingCopy
 *  org.openide.util.Exceptions
 */
package org.netbeans.modules.web.wicket.palette.editablefield;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
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

public class AddModel extends TreePathScanner<Void, Void> {

    private CompilationInfo info;
    private final JavaSource source;
    private final String wicketId;
    private final String initValue;

    public AddModel(JavaSource source, CompilationInfo info, String wicketId, String initValue) {
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

                    public void run(WorkingCopy workingCopy) throws IOException {
                        workingCopy.toPhase(JavaSource.Phase.PARSED);
                        workingCopy.rewrite((Tree)constructor.getBody(), (Tree)workingCopy.getTreeMaker().addBlockStatement(constructor.getBody(), (StatementTree)AddModel.this.getVariableTree2(workingCopy)));
                    }
                };
                try {
                    ModificationResult result = this.source.runModificationTask((Task)task1);
                    result.commit();
                    continue;
                } catch (IOException ex) {
                    Exceptions.printStackTrace((Throwable)ex);
                }
            }
        }
        return null;
    }

    private ExpressionStatementTree getVariableTree2(WorkingCopy workingCopy) {
        TreeMaker make = workingCopy.getTreeMaker();
        ExpressionStatementTree est = make.ExpressionStatement((ExpressionTree)make.MethodInvocation(Collections.emptyList(), (ExpressionTree)make.Identifier((CharSequence)"setDefaultModel"), Collections.singletonList(make.Identifier((CharSequence)"new CompoundPropertyModel(this)"))));
        return est;
    }

}
