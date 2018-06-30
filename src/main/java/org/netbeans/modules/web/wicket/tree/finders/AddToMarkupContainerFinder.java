/*
 * Some license issues have still to be clarified, especially for the "borrowed"
 * package, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.tree.finders;

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
 * This class uses a sub-scanner, AssignmentTracer, for still unknown reasons.<br>
 * TODO: For which reasons AssignmentTracer is used?
 *
 * @author Geertjan Wielenga
 */
public final class AddToMarkupContainerFinder extends TreeScanner<Void, List<Invocation>> {

    /**
     * The CompilationController gets information about Java sources from the
     * compiler.
     */
    private final CompilationController cc;

    private final Tree scan;

    public AddToMarkupContainerFinder(CompilationController cc, Tree scan) {
        this.cc = cc;
        this.scan = scan;
    }

    @Override
    public Void visitMethodInvocation(MethodInvocationTree t, List<Invocation> invocations) {
        boolean isMarkupContainer;
        Tree tree;
        assert (invocations != null);
        TypeElement markupContainerType = this.cc.getElements().getTypeElement("org.apache.wicket.MarkupContainer");
        TypeMirror mctType = markupContainerType.asType();
        TypeElement repeaterType = this.cc.getElements().getTypeElement("org.apache.wicket.markup.repeater.RefreshingView");
        TypeMirror repType = repeaterType.asType();
        TypeElement itemType = this.cc.getElements().getTypeElement("org.apache.wicket.markup.repeater.Item");
        TypeMirror itType = itemType.asType();
        TreePath path = TreePath.getPath(this.cc.getCompilationUnit(), (Tree)t);
        if (path == null) {
            return null;
        }
        do {
            path = path.getParentPath();
            if (path == null) {
                break;
            }
            path.getLeaf().getKind();
        } while (path.getLeaf().getKind() != Tree.Kind.CLASS);
        if (path == null) {
            return null;
        }
        TypeElement currentClassType = (TypeElement)this.cc.getTrees().getElement(path);
        Element target = currentClassType;
        Element elemForMethodInvocation;
        elemForMethodInvocation = this.cc.getTrees().getElement(TreePath.getPath(this.cc.getCompilationUnit(), (Tree)t));
        TypeMirror typeCallOccursOn = null;
        TypeElement enclosing = this.cc.getElementUtilities().enclosingTypeElement(elemForMethodInvocation);
        Object object = enclosing == null ? (target == null ? null : target.asType()) : (typeCallOccursOn = enclosing.asType());
        if (target == null) {
            System.err.println("Give up on " + t);
            return null;
        }
        String call; // = t.getMethodSelect().toString();
        ExpressionTree mst = t.getMethodSelect();
        ExpressionTree thingAddIsCalledOn = null;
        boolean isAddToRepeaterItem = false;
        switch (mst.getKind()) {
            case MEMBER_SELECT: {
                MemberSelectTree m = (MemberSelectTree)mst;
                String id = m.getIdentifier().toString();
                call = id;
                thingAddIsCalledOn = m.getExpression();
                if (thingAddIsCalledOn != null) {
                    target = this.cc.getTrees().getElement(TreePath.getPath(this.cc.getCompilationUnit(), (Tree)thingAddIsCalledOn));
                    if (target == null) {
                        System.err.println("Give up on " + t);
                        return null;
                    }
                    isAddToRepeaterItem = itType.equals(target.asType());
                    if (!isAddToRepeaterItem) {
                        break;
                    }
                    TreePath pathToParent = TreePath.getPath(this.cc.getCompilationUnit(), (Tree)m);
                    while ((pathToParent = pathToParent.getParentPath()) != null && pathToParent.getLeaf().getKind() != Tree.Kind.CLASS) {
                    }
                    if (pathToParent == null) {
                        break;
                    }
                    Element nuTarget = this.cc.getTrees().getElement(pathToParent);
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
                System.err.println("mst not a MemberSelectTree, it is " + (Object)((Object)mst.getKind()) + " " + mst.getClass().getName() + ": " + Arrays.asList(mst.getClass().getInterfaces()) + " : " + mst);
                return null;
            }
        }
        System.err.println("isAddToRepeaterItem " + isAddToRepeaterItem + " for " + t);
        if (thingAddIsCalledOn == null && path.getLeaf() instanceof ExpressionTree) {
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
                isMarkupContainer = this.cc.getTypeUtilities().isCastable(typeOfTarget, mctType);
            }
        } catch (IllegalArgumentException a) {
            System.err.println("isCastable throws assertion error on " + target.asType() + " and " + mctType);
            return null;
        }
        boolean isAdd = "add".equals(call);
        if (!isMarkupContainer || !isAdd) {
            return null;
        }
        List<? extends ExpressionTree> l = t.getArguments();
        if (l.size() <= 2) {
            for (ExpressionTree arg : l) {
                NewClassTree constructionOfAddedComponent;
                if (!Utils.isWebMarkupContainer(this.cc.getTrees(), this.cc.getTypes(), this.cc.getCompilationUnit(), arg)) {
                    continue;
                }
                Element argument = this.cc.getTrees().getElement(TreePath.getPath(this.cc.getCompilationUnit(), (Tree)arg));
                long start = this.cc.getTrees().getSourcePositions().getStartPosition(this.cc.getCompilationUnit(), t);
                long end = this.cc.getTrees().getSourcePositions().getEndPosition(this.cc.getCompilationUnit(), t);
                NewClassTree newClassTree = constructionOfAddedComponent = arg instanceof NewClassTree ? (NewClassTree)arg : null;
                if (constructionOfAddedComponent == null) {
                    HashSet assignedTos = new HashSet();
                    AssignmentTracer tracer = new AssignmentTracer(this.cc, this.scan, argument);
                    Tree argTree = this.cc.getTrees().getTree(argument);
                    if (argTree == null) {
                        System.err.println("ArgTree for " + argument + " null");
                        continue;
                    }
                    argTree.accept(tracer, assignedTos);
                    if (!assignedTos.isEmpty()) {
                        if (assignedTos.size() > 1) {
                            System.err.println("Multiple assignments to " + l + " - analysis will be bad");
                        }
                        constructionOfAddedComponent = (NewClassTree)assignedTos.iterator().next();
                    }
                }
                NewClassTree constructionOfParentComponent = null;
                if (thingAddIsCalledOn instanceof IdentifierTree) {
                    HashSet assignedTos = new HashSet();
                    Element el = this.cc.getTrees().getElement(TreePath.getPath(this.cc.getCompilationUnit(), (Tree)thingAddIsCalledOn));
                    Tree newTree = this.cc.getTrees().getTree(el);
                    AssignmentTracer tracer = new AssignmentTracer(this.cc, this.scan, el);
                    newTree.accept(tracer, assignedTos);
                    if (!assignedTos.isEmpty()) {
                        if (assignedTos.size() > 1) {
                            System.err.println("Multiple assignments to " + thingAddIsCalledOn + " - analysis will be bad");
                        }
                        constructionOfParentComponent = (NewClassTree)assignedTos.iterator().next();
                    }
                } else {
                    System.err.println("no thing add is called on " + thingAddIsCalledOn + " for " + t);
                }
                if (constructionOfParentComponent == null) {
                    ConstructionFinder finder = new ConstructionFinder(this.cc, typeOfTarget);
                    HashSet constructions = new HashSet();
                    this.scan.accept(finder, constructions);
                    if (constructions.size() > 1) {
                        System.err.println("More than one construction of a " + typeOfTarget + " - analysis may be bad");
                    }
                    if (!constructions.isEmpty()) {
                        constructionOfParentComponent = (NewClassTree)constructions.iterator().next();
                    }
                }
                Invocation inv = new Invocation(typeCallOccursOn, t, target, argument, constructionOfAddedComponent, constructionOfParentComponent, start, end);
                invocations.add(inv);
                break;
            }
        }
        if (elemForMethodInvocation != null && (tree = this.cc.getTrees().getTree(elemForMethodInvocation)) != null && !tree.equals(t)) {
            tree.accept(this, invocations);
        }
        Void result = (Void)super.visitMethodInvocation(t, invocations);
        return result;
    }
}
