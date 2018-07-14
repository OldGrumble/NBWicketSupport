/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.palette.util;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import java.io.IOException;
import java.util.Collections;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author Tim Boudreau
 */
class AddMethodToClassWorkerTask implements Task<WorkingCopy> {

    private final String methodName;
    private final String methodReturnType;
    private final String body;

    public AddMethodToClassWorkerTask(final String methodName, final String methodReturnType, final String body) {
        this.methodName = methodName;
        this.methodReturnType = methodReturnType;
        this.body = body;
    }

    @Override
    public void run(WorkingCopy workingCopy) throws IOException {
        workingCopy.toPhase(JavaSource.Phase.RESOLVED);
        CompilationUnitTree cut = workingCopy.getCompilationUnit();
        TreeMaker make = workingCopy.getTreeMaker();
        for (Tree typeDecl : cut.getTypeDecls()) {
            if (Tree.Kind.CLASS != typeDecl.getKind()) {
                continue;
            }
            ClassTree clazz = (ClassTree)typeDecl;
            ModifiersTree methodModifiers = make.Modifiers(Collections.singleton(Modifier.PUBLIC), Collections.emptyList());
            MethodTree newMethod = make.Method(methodModifiers, methodName, make.Identifier(methodReturnType), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), "{ " + body + " }", null);
            ClassTree modifiedClazz = make.addClassMember(clazz, newMethod);
            workingCopy.rewrite(clazz, modifiedClazz);
        }
    }
}
