/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.tree.finders;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.web.wicket.tree.util.Invocation;
import org.netbeans.modules.web.wicket.tree.util.Utils;

/**
 * A TreeScanner implementation for collecting all invocations of some type.<br>
 * TODO: Which types?<br>
 * <br>
 * This class uses a sub-scanner, AssignmentTracer, for still unknown
 * reasons.<br>
 * TODO: For which reasons AssignmentTracer is used?
 *
 * @author Tim Boudreau
 */
public final class AddToMarkupContainerFinder extends TreeScanner<Void, List<Invocation>> {

    /**
     * The CompilationController gets information about Java sources from the
     * compiler.
     */
    private final CompilationController cc;

    private final Tree scan;

    public AddToMarkupContainerFinder(CompilationController cc, ClassTree scan) {
        this.cc = cc;
        this.scan = scan;
    }

    @Override
    public Void visitMethodInvocation(MethodInvocationTree invocationTree, List<Invocation> invocations) {
        assert (invocations != null);
        boolean isMarkupContainer;
        // Get all sub-classes of org.apache.wicket.MarkupContainer
        TypeElement markupContainerType = cc.getElements().getTypeElement("org.apache.wicket.MarkupContainer");
        TypeMirror mctType = markupContainerType.asType();
        // Get all sub-classes of org.apache.wicket.markup.repeater.RefreshingView (unused)
        TypeElement repeaterType = cc.getElements().getTypeElement("org.apache.wicket.markup.repeater.RefreshingView");
        TypeMirror repType = repeaterType.asType();
        // Get all sub-classes of org.apache.wicket.markup.repeater.Item
        TypeElement itemType = cc.getElements().getTypeElement("org.apache.wicket.markup.repeater.Item");
        TypeMirror itType = itemType.asType();
        // Get path to enclosing class
        TreePath path = TreePath.getPath(cc.getCompilationUnit(), invocationTree);
        while (path != null) {
            path = path.getParentPath();
            if (path == null || path.getLeaf().getKind() == Tree.Kind.CLASS) {
                break;
            }
        }
        if (path == null) {
            return null;
        }
        // Get the class type referenced by the path
        TypeElement currentClassType = (TypeElement)cc.getTrees().getElement(path);
        // BEGIN of what?
        Element target = currentClassType;
        if (target == null) {
            System.err.println("Give up on " + invocationTree);
            return null;
        }
        Element elemForMethodInvocation = cc.getTrees().getElement(TreePath.getPath(cc.getCompilationUnit(), invocationTree));
        TypeElement enclosing = cc.getElementUtilities().enclosingTypeElement(elemForMethodInvocation);
        TypeMirror typeCallOccursOn = (enclosing != null) ? enclosing.asType() : target.asType();

        String call; //  = invocationTree.getMethodSelect().toString();  // assigned value never used
        ExpressionTree mst = invocationTree.getMethodSelect();
        ExpressionTree thingAddIsCalledOn = null;
        boolean isAddToRepeaterItem = false;
        switch (mst.getKind()) {
            case MEMBER_SELECT: {
                MemberSelectTree m = (MemberSelectTree)mst;
                call = m.getIdentifier().toString();
                thingAddIsCalledOn = m.getExpression();
                if (thingAddIsCalledOn != null) {
                    target = cc.getTrees().getElement(TreePath.getPath(cc.getCompilationUnit(), thingAddIsCalledOn));
                    if (target == null) {
                        System.err.println("Give up on " + invocationTree);
                        return null;
                    }
                    isAddToRepeaterItem = itType.equals(target.asType());
                    if (!isAddToRepeaterItem) {
                        break;
                    }
                    TreePath pathToParent = TreePath.getPath(cc.getCompilationUnit(), m);
                    while ((pathToParent = pathToParent.getParentPath()) != null && pathToParent.getLeaf().getKind() != Tree.Kind.CLASS) {
                    }
                    if (pathToParent == null) {
                        break;
                    }
                    Element nuTarget = cc.getTrees().getElement(pathToParent);
                    System.err.println("Add To Repeater parent proxied to " + nuTarget);
                    if (nuTarget == null) {
                        break;
                    }
                    target = nuTarget;
                    break;
                }
                target = currentClassType;
                break;
            }
            case IDENTIFIER: {
                IdentifierTree idTree = (IdentifierTree)mst;
                target = currentClassType;
                call = idTree.toString();
                break;
            }
            default: {
                System.err.println("mst not a MemberSelectTree, it is " + mst.getKind() + " " + mst.getClass().getName() + ": " + Arrays.asList(mst.getClass().getInterfaces()) + " : " + mst);
                return null;
            }
        }
        System.err.println("isAddToRepeaterItem " + isAddToRepeaterItem + " for " + invocationTree);
        if (thingAddIsCalledOn == null && (path.getLeaf() instanceof ExpressionTree)) {
            thingAddIsCalledOn = (ExpressionTree)path.getLeaf();
        } else if (isAddToRepeaterItem) {
            // empty if block
        }

        if (target == null) {
            System.err.println("Reverting current class type");
            target = currentClassType;
        }
        TypeMirror typeOfTarget;
        try {
            if (target == null) {
                typeOfTarget = null;
                isMarkupContainer = false;
            } else {
                typeOfTarget = target.asType();
                isMarkupContainer = cc.getTypeUtilities().isCastable(typeOfTarget, mctType);
            }
        } catch (IllegalArgumentException a) {
            System.err.println("isCastable throws assertion error on " + target.asType() + " and " + mctType);
            return null;
        }
        boolean isAdd = "add".equals(call);
        if (!isMarkupContainer || !isAdd) {
            return null;
        }
        List<? extends ExpressionTree> l = invocationTree.getArguments();
        if (l.size() <= 2) {
            for (ExpressionTree exprTree : l) {
                if (Utils.isWebMarkupContainer(cc.getTrees(), cc.getTypes(), cc.getCompilationUnit(), exprTree)) {
                    boolean consumed = handleWebContainer(invocationTree, l, exprTree, typeCallOccursOn, typeOfTarget, target, thingAddIsCalledOn, invocations);
                    if (consumed) {
                        break;
                    }
                }
            }
        }
        if (elemForMethodInvocation != null) {
            Tree tree = cc.getTrees().getTree(elemForMethodInvocation);
            if (tree != null && !tree.equals(invocationTree)) {
                tree.accept(this, invocations);
            }
        }
        Void result = super.visitMethodInvocation(invocationTree, invocations);
        return result;
    }

    private boolean handleWebContainer(
            MethodInvocationTree invocationTree,
            List<? extends ExpressionTree> exprTreeList,
            ExpressionTree exprTree,
            TypeMirror typeCallOccursOn,
            TypeMirror typeOfTarget,
            Element target,
            ExpressionTree thingAddIsCalledOn,
            List<Invocation> invocations
    ) {
        Element argument = cc.getTrees().getElement(TreePath.getPath(cc.getCompilationUnit(), exprTree));
        NewClassTree constructionOfAddedComponent = getConstructionOfAddedComponent(exprTreeList, exprTree, argument);
        if (constructionOfAddedComponent != null) {
            long start = cc.getTrees().getSourcePositions().getStartPosition(cc.getCompilationUnit(), invocationTree);
            long end = cc.getTrees().getSourcePositions().getEndPosition(cc.getCompilationUnit(), invocationTree);
            NewClassTree constructionOfParentComponent = getConstructionOfParentComponent(invocationTree, typeOfTarget, thingAddIsCalledOn);
            Invocation inv = new Invocation(typeCallOccursOn, invocationTree, target, argument, constructionOfAddedComponent, constructionOfParentComponent, start, end);
            invocations.add(inv);
            return true;
        }
        return false;
    }

    private NewClassTree getConstructionOfAddedComponent(
            List<? extends ExpressionTree> exprTreeList,
            ExpressionTree exprTree,
            Element argument
    ) {
        NewClassTree constructionOfAddedComponent = (exprTree instanceof NewClassTree) ? (NewClassTree)exprTree : null;
        if (constructionOfAddedComponent == null) {
            HashSet<NewClassTree> assignedTos = new HashSet<>();
            AssignmentTracer tracer = new AssignmentTracer(cc, scan, argument);
            Tree argTree = cc.getTrees().getTree(argument);
            if (argTree == null) {
                System.err.println("ArgTree for " + argument + " null");
            } else {
                argTree.accept(tracer, assignedTos);
                if (!assignedTos.isEmpty()) {
                    if (assignedTos.size() > 1) {
                        System.err.println("Multiple assignments to " + exprTreeList + " - analysis will be bad");
                    }
                    constructionOfAddedComponent = assignedTos.iterator().next();
                }
            }
        }
        return constructionOfAddedComponent;
    }

    private NewClassTree getConstructionOfParentComponent(
            MethodInvocationTree invocationTree,
            TypeMirror typeOfTarget,
            ExpressionTree thingAddIsCalledOn
    ) {
        NewClassTree constructionOfParentComponent = null;
        if (thingAddIsCalledOn instanceof IdentifierTree) {
            HashSet<NewClassTree> assignedTos = new HashSet<>();
            Element el = cc.getTrees().getElement(TreePath.getPath(cc.getCompilationUnit(), thingAddIsCalledOn));
            Tree newTree = cc.getTrees().getTree(el);
            AssignmentTracer tracer = new AssignmentTracer(cc, scan, el);
            newTree.accept(tracer, assignedTos);
            if (!assignedTos.isEmpty()) {
                if (assignedTos.size() > 1) {
                    System.err.println("Multiple assignments to " + thingAddIsCalledOn + " - analysis will be bad");
                }
                constructionOfParentComponent = assignedTos.iterator().next();
            }
        } else {
            System.err.println("no thing add is called on " + thingAddIsCalledOn + " for " + invocationTree);
        }
        if (constructionOfParentComponent == null) {
            ConstructionFinder finder = new ConstructionFinder(cc, typeOfTarget);
            HashSet<NewClassTree> constructions = new HashSet<>();
            this.scan.accept(finder, constructions);
            if (constructions.size() > 1) {
                System.err.println("More than one construction of a " + typeOfTarget + " - analysis may be bad");
            }
            if (!constructions.isEmpty()) {
                constructionOfParentComponent = constructions.iterator().next();
            }
        }
        return constructionOfParentComponent;
    }
}
