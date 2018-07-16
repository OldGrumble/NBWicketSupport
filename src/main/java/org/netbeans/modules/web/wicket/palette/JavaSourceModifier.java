package org.netbeans.modules.web.wicket.palette;

import com.sun.source.util.TreePathScanner;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.Task;

/**
 *
 * @author Peter Nabbefeld
 */
public abstract class JavaSourceModifier<T> {

    private final TreePathScanner<Void, T> tpScanner;
    private final Task<CompilationController> worker;

    public JavaSourceModifier(TreePathScanner<Void, T> tpScanner, Task<CompilationController> worker) {
        this.tpScanner = tpScanner;
        this.worker = worker;
    }

}
