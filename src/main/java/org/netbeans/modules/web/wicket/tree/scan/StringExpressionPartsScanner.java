/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.tree.scan;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.web.wicket.tree.results.TreeScanResultHandler;

/**
 * This class scans a MethodTree for constructors.
 *
 * @author Tim Boudreau
 * @author Peter Nabbefeld
 */
public class StringExpressionPartsScanner extends AbstractTreeScanner<String> {

    public StringExpressionPartsScanner(CompilationInfo info) {
        super(info);
    }

    @Override
    public Void visitIdentifier(IdentifierTree tree, TreeScanResultHandler<String> resultHandler) {
        TreePath path = TreePath.getPath(info.getCompilationUnit(), tree);
        Trees trees = info.getTrees();
        TypeMirror type = trees.getTypeMirror(path);
        if ("java.lang.String".equals(type.toString())) {
            Element expressionElement = trees.getElement(path);
            Tree elementTree = trees.getTree(expressionElement);
            if (elementTree instanceof VariableTree) {
                VariableTree vt = (VariableTree)elementTree;
                vt.accept(this, resultHandler);
            }
        }
        return (Void)super.visitIdentifier(tree, resultHandler);
    }

    @Override
    public Void visitLiteral(LiteralTree tree, TreeScanResultHandler<String> resultHandler) {
        resultHandler.handleResult(tree.toString());
        return (Void)super.visitLiteral(tree, resultHandler);
    }
}
