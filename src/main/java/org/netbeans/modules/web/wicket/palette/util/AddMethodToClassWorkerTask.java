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
        // Resolve working copy, so trees are constructed
        workingCopy.toPhase(JavaSource.Phase.RESOLVED);
        // Get the root
        CompilationUnitTree cut = workingCopy.getCompilationUnit();
        TreeMaker treeMaker = workingCopy.getTreeMaker();
        for (Tree typeDecl : cut.getTypeDecls()) {
            if (typeDecl.getKind() == Tree.Kind.CLASS) {
                // Tree of classes found
                ClassTree clazz = (ClassTree)typeDecl;
                ModifiersTree methodModifiers = treeMaker.Modifiers(
                        // Flags
                        Collections.singleton(Modifier.PUBLIC),
                        // Annotations
                        Collections.emptyList()
                );
                MethodTree newMethod = treeMaker.Method(
                        // Modifiers (see above)
                        methodModifiers,
                        methodName,
                        treeMaker.Identifier(methodReturnType),
                        // Parameter types
                        Collections.emptyList(),
                        // Parameter values
                        Collections.emptyList(),
                        // Throws clauses
                        Collections.emptyList(),
                        // Method body
                        "{ " + body + " }",
                        // Default value (in case of annotations)
                        null
                );
                ClassTree modifiedClazz = treeMaker.addClassMember(clazz, newMethod);
                workingCopy.rewrite(clazz, modifiedClazz);
            }
        }
    }
}
