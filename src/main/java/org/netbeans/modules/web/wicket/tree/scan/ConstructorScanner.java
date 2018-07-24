/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.tree.scan;

import com.sun.source.tree.MethodTree;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.web.wicket.tree.results.TreeScanResultHandler;

/**
 * This class scans a MethodTree for constructors.
 *
 * @author Tim Boudreau
 * @author Peter Nabbefeld
 */
public class ConstructorScanner extends AbstractTreeScanner<MethodTree> {

    public ConstructorScanner(CompilationInfo info) {
        super(info);
    }

    @Override
    public Void visitMethod(MethodTree mt, TreeScanResultHandler<MethodTree> resultHandler) {
        if ("<init>".equals(mt.getName().toString())) {
            resultHandler.handleResult(mt);
        }
        return (Void)super.visitMethod(mt, resultHandler);
    }
}
