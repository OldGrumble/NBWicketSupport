/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.web.wicket.tree.scan;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.web.wicket.tree.results.CollectingResultHandler;
import org.netbeans.modules.web.wicket.tree.results.TreeScanResultHandler;

public class AbstractTreeScannerForIds extends AbstractTreeScanner<String> {

    public AbstractTreeScannerForIds(CompilationInfo info) {
        super(info);
    }

    /**
     * Accept constructor calls.
     *
     * @param tree Tree
     * @param resultHandler Handler for Ids
     * @return nothing
     */
    @Override
    public Void visitNewClass(NewClassTree tree, TreeScanResultHandler<String> resultHandler) {
        List<? extends ExpressionTree> args = tree.getArguments();
        scanArgsForComponentIds(args, resultHandler);
        if (args.isEmpty()) {
            TypeMirror classType = info.getTrees().getTypeMirror(TreePath.getPath(info.getCompilationUnit(), (Tree)tree));
            TypeElement el = (TypeElement)info.getTypes().asElement(classType);
            if (el != null) {
                System.err.println("Super element is " + el.getQualifiedName());
                ClassTree cTree = info.getTrees().getTree(el);

                CollectingResultHandler<MethodTree, Set<MethodTree>> constructorHandler = (CollectingResultHandler<MethodTree, Set<MethodTree>>)new CollectingResultHandler(Set.class);
                cTree.accept(new ConstructorScanner(info), constructorHandler);
                Set<MethodTree> constructors = constructorHandler.getResult();

                SuperclassConstructorInvocationScanner scis = new SuperclassConstructorInvocationScanner(info);
                for (MethodTree constructor : constructors) {
                    constructor.accept(scis, resultHandler);
                }

            } else {
                System.err.println("Super element is null");
            }
        }
        return (Void)super.visitNewClass(tree, resultHandler);
    }

    /**
     * Scan arguments in constructor calls for component ids. As these are
     * strings, it's okay to set the type of the result handler to
     * TreeScanResultHandler&lt;String&gt;.
     *
     * @param args The arguments list.
     * @param resultHandler The result handler.
     */
    protected void scanArgsForComponentIds(List<? extends ExpressionTree> args, TreeScanResultHandler<String> resultHandler) {
        StringExpressionPartsScanner ses = new StringExpressionPartsScanner(info);
        for (ExpressionTree expTree : args) {
            expTree.accept(ses, resultHandler);
        }
    }
}
