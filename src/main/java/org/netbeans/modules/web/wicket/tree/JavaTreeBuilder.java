/*
 * Decompiled with CFR 0_130.
 * 
 * Could not load the following classes:
 *  org.netbeans.api.java.source.CompilationController
 *  org.netbeans.api.java.source.JavaSource
 *  org.netbeans.api.java.source.JavaSource$Phase
 *  org.netbeans.api.java.source.Task
 *  org.openide.filesystems.FileObject
 *  org.openide.util.Exceptions
 */
package org.netbeans.modules.web.wicket.tree;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.web.wicket.tree.finders.AddToMarkupContainerFinder;
import org.netbeans.modules.web.wicket.tree.finders.ComponentConstructorInvocationFinder;
import org.netbeans.modules.web.wicket.tree.finders.ComponentIdFinder;
import org.netbeans.modules.web.wicket.tree.util.Invocation;
import org.netbeans.modules.web.wicket.tree.util.ProblemFinderVisitor;
import org.netbeans.modules.web.wicket.tree.finders.TreeCallback;
import org.netbeans.modules.web.wicket.tree.util.Utils;
import org.netbeans.modules.web.wicket.util.StringUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * JavaSource: Class representing Java source file opened in the (NetBeans)
 * editor.
 *
 * @author Geertjan Wielenga
 */
public final class JavaTreeBuilder {

    private final FileObject file;
    private final Document doc;
    private Collection<ProblemFinderVisitor.Problem> problems;

    /**
     * Create a JavaTreeBuilder for a FileObject. A JavaSource will be
     * instantiated from the FileObject for analyzing.
     *
     * @param file The FileObject.
     */
    public JavaTreeBuilder(FileObject file) {
        this.file = file;
        assert (file != null);
        this.doc = null;
    }

    /**
     * Create a JavaTreeBuilder for a Document. A JavaSource will be
     * instantiated from the FileObject for analyzing.
     *
     * @param doc The Document.
     */
    public JavaTreeBuilder(Document doc) {
        this.doc = doc;
        assert (doc != null);
        this.file = null;
    }

    /**
     * Analyze the underlying FileObject or Document.
     *
     * @param treeCallback An interface allowing to report intermediary results.
     */
    public void analyze(TreeCallback treeCallback) {
        this.analyze(treeCallback, null, false);
    }

    /**
     * Analyze the underlying FileObject or Document.
     *
     * @param treeCallback An interface allowing to report intermediary results.
     * @param visitors A collection of ProblemFinderVisitors.
     * @param immediate A flag indicating if finishing of scans should be waited
     * for.
     */
    @SuppressWarnings("null")
    public void analyze(TreeCallback treeCallback, Collection<ProblemFinderVisitor> visitors, boolean immediate) {
        try {
            JavaSource src = this.file == null ? JavaSource.forDocument((Document)this.doc) : JavaSource.forFileObject((FileObject)this.file);
            if (immediate) {
                src.runUserActionTask((Task)new TreeWalker(treeCallback, visitors), true);
            } else {
                src.runWhenScanFinished((Task)new TreeWalker(treeCallback, visitors), true);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace((Throwable)ex);
        }
    }

    public Collection<? extends ProblemFinderVisitor.Problem> getProblems() {
        return this.problems == null ? Collections.emptySet() : this.problems;
    }

    private final class TreeWalker implements Task<CompilationController> {

        private final TreeCallback callback;
        private final Collection<ProblemFinderVisitor> visitors;

        TreeWalker(TreeCallback t, Collection<ProblemFinderVisitor> visitors) {
            this.callback = t;
            this.visitors = visitors == null ? Collections.emptySet() : visitors;
            JavaTreeBuilder.this.problems = visitors == null ? null : new HashSet();
        }

        @Override
        public void run(CompilationController cc) throws Exception {
            cc.toPhase(JavaSource.Phase.RESOLVED);
            TypeElement markupContainerType = cc.getElements().getTypeElement("org.apache.wicket.MarkupContainer");
            if (markupContainerType == null) {
                System.err.println("Wicket not found on classpath");
                return;
            }
            ArrayList<? extends Tree> types = new ArrayList<>(cc.getCompilationUnit().getTypeDecls());
            Iterator<? extends Tree> it = types.iterator();
            while (it.hasNext()) {
                Tree tree = it.next();
                TypeMirror mirror = cc.getTrees().getTypeMirror(TreePath.getPath(cc.getCompilationUnit(), tree));
                if (!(tree instanceof ClassTree) || !Utils.isWebMarkupContainer(mirror, cc.getTypes())) {
                    it.remove();
                }
            }
            HashMap<String, Node<String>> nodes = new HashMap<>();
            MarkupContainerTree<String> result = null;
            HashMap invocations = new HashMap();
            HashMap types2ids = new HashMap();
            for (Tree tree : types) {
                if (result == null) {
                    result = new MarkupContainerTree<>();
                }
                if (!this.visitors.isEmpty()) {
                    for (ProblemFinderVisitor v : this.visitors) {
                        try {
                            v.visitWicketMarkupContainer((ClassTree)tree, cc, cc.getFileObject(), JavaTreeBuilder.this.problems);
                        } catch (Exception e) {
                            Exceptions.printStackTrace((Throwable)e);
                        }
                    }
                }
                LinkedList<Invocation> invs = new LinkedList();
                invocations.put(tree, invs);
                AddToMarkupContainerFinder addFinder = new AddToMarkupContainerFinder(cc, tree);
                tree.accept(addFinder, invs);
                ComponentConstructorInvocationFinder createFinder = new ComponentConstructorInvocationFinder(cc);
                HashSet<NewClassTree> constructorInvocations = new HashSet();
                tree.accept(createFinder, constructorInvocations);
                HashMap invs2ids = new HashMap();
                types2ids.put(tree, invs2ids);
                ComponentIdFinder idFinder = new ComponentIdFinder(cc);
                constructorInvocations.forEach((invocation) -> {
                    ArrayList ids = new ArrayList(1);
                    invs2ids.put(invocation, ids);
                    invocation.accept(idFinder, ids);
                    if (!(this.visitors.isEmpty())) {
                        for (ProblemFinderVisitor v : this.visitors) {
                            try {
                                v.visitWicketComponentConstruction(invocation, cc, cc.getFileObject(), ids, JavaTreeBuilder.this.problems);
                            } catch (Exception e) {
                                Exceptions.printStackTrace((Throwable)e);
                            }
                        }
                    }
                });
                Collections.sort(invs);
                for (Invocation inv : invs) {
                    if (!this.visitors.isEmpty()) {
                        for (ProblemFinderVisitor v : this.visitors) {
                            try {
                                v.visitWicketAddInvocation(inv, cc, cc.getFileObject(), JavaTreeBuilder.this.problems);
                            } catch (Exception e) {
                                Exceptions.printStackTrace((Throwable)e);
                            }
                        }
                    }
                    NewClassTree parent = inv.getParent();
                    NewClassTree child = inv.getSrc();
                    List parentIds = parent == null ? null : (List)invs2ids.get(parent);
                    List childIds = child == null ? Collections.singletonList("?") : (List)invs2ids.get(child);
                    String cid = childIds == null || childIds.isEmpty() ? "Unknown id" : StringUtils.unquote((String)childIds.get(0));
                    String pid = parentIds == null || parentIds.isEmpty() ? null : StringUtils.unquote((String)parentIds.get(0));
                    System.err.println(childIds + " added to " + parentIds);
                    System.err.println(inv.getArgument() + " added to " + inv.getTarget());
                    Node<String> n = new NodeImpl<>(cid, null, (int)inv.getStart(), cid);
                    nodes.put(cid, n);
                    if (pid != null) {
                        Node<String> parNode = (Node<String>)nodes.get(pid);
                        if (parNode == null) {
                            parNode = new NodeImpl<>(pid, null, (int)inv.getStart(), cid);
                            nodes.put(pid, parNode);
                        }
                        ((NodeImpl)parNode).add(n);
                    }
                }
                for (Node node : nodes.values()) {
                    if (((NodeImpl)node).getParent() != null) {
                        continue;
                    }
                    result.add(node);
                }
            }
            if (this.callback != null) {
                this.callback.setTree(result);
            }
        }
    }
}
