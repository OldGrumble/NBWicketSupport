/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.palette.util;

import com.sun.source.util.TreePathScanner;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;

/**
 *
 * @author Tim Boudreau
 */
public class AddInvocationToConstructorTask implements Task<CompilationController> {

    private final JavaSource source;
    private final String javaText;

    public AddInvocationToConstructorTask(final JavaSource source, final String javaText) {
        this.source = source;
        this.javaText = javaText;
    }

    @Override
    public void run(CompilationController compilationController) throws Exception {
        compilationController.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);

        TreePathScanner<Void, Void> scanner = new AddInvocationToConstructor(
                source,
                compilationController,
                javaText
        );
        scanner.scan(compilationController.getCompilationUnit(), null);
    }
}
