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
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.web.wicket.tree.AddToMarkupContainerFinder;
import org.netbeans.modules.web.wicket.tree.ComponentConstructorInvocationFinder;
import org.netbeans.modules.web.wicket.tree.ComponentIdFinder;
import org.netbeans.modules.web.wicket.tree.Invocation;
import org.netbeans.modules.web.wicket.tree.MarkupContainerTree;
import org.netbeans.modules.web.wicket.tree.ProblemFinderVisitor;
import org.netbeans.modules.web.wicket.tree.Utils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

public final class JavaTreeBuilder {
    private final FileObject file;
    private final Document doc;
    private Collection<ProblemFinderVisitor.Problem> problems;

    public JavaTreeBuilder(FileObject file) {
        this.file = file;
        assert (file != null);
        this.doc = null;
    }

    public JavaTreeBuilder(Document doc) {
        this.doc = doc;
        assert (doc != null);
        this.file = null;
    }

    public void analyze(TreeCallback t) {
        this.analyze(t, null, false);
    }

    @SuppressWarnings("null")
    public void analyze(TreeCallback t, Collection<ProblemFinderVisitor> visitors, boolean immediate) {
        try {
            JavaSource src;
            JavaSource javaSource = src = this.file == null ? JavaSource.forDocument((Document)this.doc) : JavaSource.forFileObject((FileObject)this.file);
            if (immediate) {
                src.runUserActionTask((Task)new T(t, visitors), true);
            } else {
                src.runWhenScanFinished((Task)new T(t, visitors), true);
            }
        }
        catch (IOException ex) {
            Exceptions.printStackTrace((Throwable)ex);
        }
    }

    public Collection<? extends ProblemFinderVisitor.Problem> getProblems() {
        return this.problems == null ? Collections.emptySet() : this.problems;
    }

    static String unquote(String quoted) {
        if (quoted == null) {
            return "?";
        }
        char c = quoted.charAt(0);
        if (c == '\"' || c == '\'') {
            quoted = quoted.substring(1);
        }
        if ((c = quoted.charAt(quoted.length() - 1)) == '\"' || c == '\'') {
            quoted = quoted.substring(0, quoted.length() - 1);
        }
        return quoted;
    }

    private final class T
    implements Task<CompilationController> {
        private final TreeCallback callback;
        private final Collection<ProblemFinderVisitor> visitors;

        T(TreeCallback t, Collection<ProblemFinderVisitor> visitors) {
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
                if (tree instanceof ClassTree && Utils.isWebMarkupContainer(mirror, cc.getTypes())) continue;
                it.remove();
            }
            HashMap<String, MarkupContainerTree.N<String>> nodes = new HashMap<>();
            MarkupContainerTree<String> result = null;
            HashMap invocations = new HashMap();
            HashMap types2ids = new HashMap();
            for (Tree tree : types) {
                if (result == null) {
                    result = new MarkupContainerTree<>(new MarkupContainerTree.N<>("root", null, 0, null));
                }
                if (!this.visitors.isEmpty()) {
                    for (ProblemFinderVisitor v : this.visitors) {
                        try {
                            v.visitWicketMarkupContainer((ClassTree)tree, cc, cc.getFileObject(), JavaTreeBuilder.this.problems);
                        }
                        catch (Exception e) {
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
                            }
                            catch (Exception e) {
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
                            }
                            catch (Exception e) {
                                Exceptions.printStackTrace((Throwable)e);
                            }
                        }
                    }
                    NewClassTree parent = inv.getParent();
                    NewClassTree child = inv.getSrc();
                    List parentIds = parent == null ? null : (List)invs2ids.get(parent);
                    List childIds = child == null ? Collections.singletonList("?") : (List)invs2ids.get(child);
                    String cid = childIds == null || childIds.isEmpty() ? "Unknown id" : JavaTreeBuilder.unquote((String)childIds.get(0));
                    String pid = parentIds == null || parentIds.isEmpty() ? null : JavaTreeBuilder.unquote((String)parentIds.get(0));
                    System.err.println(childIds + " added to " + parentIds);
                    System.err.println(inv.getArgument() + " added to " + inv.getTarget());
                    MarkupContainerTree.N<String> n = new MarkupContainerTree.N<>(cid, null, (int)inv.getStart(), cid);
                    nodes.put(cid, n);
                    if (pid == null) continue;
                    MarkupContainerTree.N<String> parNode = (MarkupContainerTree.N<String>)nodes.get(pid);
                    if (parNode == null) {
                        parNode = new MarkupContainerTree.N<>(pid, null, (int)inv.getStart(), cid);
                        nodes.put(pid, parNode);
                    }
                    parNode.add(n);
                }
                for (MarkupContainerTree.N n : nodes.values()) {
                    if (n.getParent() != null) continue;
                    result.add(n);
                }
            }
            if (this.callback != null) {
                this.callback.setTree(result);
            }
        }
    }

    public static interface TreeCallback {
        public void setTree(MarkupContainerTree<String> var1);
    }

}

