/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.tree.scan;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.web.wicket.tree.results.TreeScanResultHandler;

/**
 * This class scans a MethodTree for constructors.
 *
 * @author Tim Boudreau
 * @author Peter Nabbefeld
 */
public class SuperclassConstructorInvocationScanner extends AbstractTreeScannerForIds {

    public SuperclassConstructorInvocationScanner(CompilationInfo info) {
        super(info);
    }

    @Override
    public Void visitMethodInvocation(MethodInvocationTree tree, TreeScanResultHandler<String> resultHandler) {
        if (isSuperCallInConstructor(tree)) {
            List<? extends ExpressionTree> args = tree.getArguments();
            this.scanArgsForComponentIds(args, resultHandler);
        }
        return (Void)super.visitMethodInvocation(tree, resultHandler);
    }

    private boolean isSuperCallInConstructor(MethodInvocationTree tree) {
        return tree.toString().startsWith("super");
    }
}
