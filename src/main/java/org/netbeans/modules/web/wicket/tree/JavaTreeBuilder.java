/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.tree;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.NewClassTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
import org.netbeans.modules.web.wicket.tree.results.CollectingResultHandler;
import org.netbeans.modules.web.wicket.tree.scan.ComponentIdScanner;
import org.netbeans.modules.web.wicket.tree.util.WicketCompilationUtils;
import org.netbeans.modules.web.wicket.util.StringUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * JavaSource: Class representing Java source file opened in the (NetBeans)
 * editor.
 *
 * @author Tim Boudreau
 */
public final class JavaTreeBuilder {

    public static enum ScannerUsage {

        CLASSIC_FINDER,
        REFACTORED_SCANNER;
    }

    public static final ScannerUsage SCANNER_USAGE = ScannerUsage.CLASSIC_FINDER;

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
                src.runUserActionTask(new JavaTreeWalker(treeCallback, visitors), true);
            } else {
                src.runWhenScanFinished(new JavaTreeWalker(treeCallback, visitors), true);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace((Throwable)ex);
        }
    }

    public Collection<? extends ProblemFinderVisitor.Problem> getProblems() {
        return problems == null ? Collections.emptySet() : problems;
    }

    private final class JavaTreeWalker implements Task<CompilationController> {

        private final TreeCallback callback;
        private final Collection<ProblemFinderVisitor> visitors;

        JavaTreeWalker(TreeCallback callback, Collection<ProblemFinderVisitor> visitors) {
            this.callback = callback;
            this.visitors = visitors == null ? Collections.emptySet() : visitors;
            JavaTreeBuilder.this.problems = visitors == null ? null : new HashSet();
        }

        @Override
        public void run(CompilationController cc) throws Exception {
            // Check Wicket available
            if (!WicketCompilationUtils.isWicketOnClasspath(cc)) {
                return;
            }
            // Get class trees (usually one)
            List<ClassTree> types = WicketCompilationUtils.getClassTrees(cc);
            // Initialize analyzer maps
            HashMap<String, Node<String>> nodes = new HashMap<>();
            MarkupContainerTree<String> result = null;
            HashMap<ClassTree, LinkedList<Invocation>> invocations = new HashMap<>();
            HashMap<ClassTree, HashMap<NewClassTree, List<String>>> types2ids = new HashMap<>();
            // Process all class trees
            for (ClassTree tree : types) {
                if (result == null) {
                    result = new MarkupContainerTree<>();
                }
                // <editor-fold defaultstate="collapsed" desc="visitors handling (unused)">                          
                if (!visitors.isEmpty()) {
                    for (ProblemFinderVisitor v : visitors) {
                        try {
                            v.visitWicketMarkupContainer(tree, cc, cc.getFileObject(), JavaTreeBuilder.this.problems);
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }// </editor-fold>
                ComponentConstructorInvocationFinder createFinder = new ComponentConstructorInvocationFinder(cc);
                HashSet<NewClassTree> constructorInvocations = new HashSet();
                tree.accept(createFinder, constructorInvocations);

                LinkedList<Invocation> invs = new LinkedList();
                invocations.put(tree, invs);

                AddToMarkupContainerFinder addFinder = new AddToMarkupContainerFinder(cc, tree);
                tree.accept(addFinder, invs);

                HashMap<NewClassTree, List<String>> invs2ids = new HashMap();
                types2ids.put(tree, invs2ids);

                // Get every Id found per constructor invocation.
                switch (SCANNER_USAGE) {
                    case CLASSIC_FINDER: {
                        ComponentIdFinder idFinder = new ComponentIdFinder(cc);
                        constructorInvocations.forEach((invocation) -> {
                            List<String> ids = new ArrayList<>(1);
                            invs2ids.put(invocation, ids);
                            invocation.accept(idFinder, ids);
                            // <editor-fold defaultstate="collapsed" desc="visitors handling (unused)">                          
                            if (!(visitors.isEmpty())) {
                                for (ProblemFinderVisitor v : visitors) {
                                    try {
                                        v.visitWicketComponentConstruction(invocation, cc, cc.getFileObject(), ids, JavaTreeBuilder.this.problems);
                                    } catch (Exception ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                }
                            }// </editor-fold>
                        });
                        break;
                    }
                    case REFACTORED_SCANNER: {
                        List<String> ids = new ArrayList<>(1);
                        CollectingResultHandler<String, List<String>> constructorHandler = (CollectingResultHandler<String, List<String>>)new CollectingResultHandler(ids);
                        ComponentIdScanner idScanner = new ComponentIdScanner(cc);
                        constructorInvocations.forEach((invocation) -> {
                            invocation.accept(idScanner, constructorHandler);
                            // <editor-fold defaultstate="collapsed" desc="visitors handling (unused)">                          
                            if (!visitors.isEmpty()) {
                                for (ProblemFinderVisitor v : visitors) {
                                    try {
                                        v.visitWicketComponentConstruction(invocation, cc, cc.getFileObject(), ids, JavaTreeBuilder.this.problems);
                                    } catch (Exception ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                }
                            }// </editor-fold>
                            invs2ids.put(invocation, new ArrayList<>(ids));
                            ids.clear();
                        });
                        break;
                    }
                    default:
                        throw new AssertionError();
                }

                // Sort added components (found by AddToMarkupContainerFinder)
                Collections.sort(invs);
                // Guess: For every invocation of "add" search a "new" contained
                // like "add(new Label("theId"))"
                // This will not find "x = new Label("theId"); add(x);"
                for (Invocation inv : invs) {
                    // <editor-fold defaultstate="collapsed" desc="visitors handling (unused)">                          
                    if (!visitors.isEmpty()) {
                        for (ProblemFinderVisitor v : visitors) {
                            try {
                                v.visitWicketAddInvocation(inv, cc, cc.getFileObject(), JavaTreeBuilder.this.problems);
                            } catch (Exception ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }// </editor-fold>
                    NewClassTree parent = inv.getParent();
                    NewClassTree child = inv.getSrc();
                    List<String> parentIds = parent == null ? null : invs2ids.get(parent);
                    List<String> childIds = child == null ? Collections.singletonList("?") : invs2ids.get(child);
                    String cid = childIds == null || childIds.isEmpty() ? "Unknown id" : StringUtils.unquote(childIds.get(0));
                    String pid = parentIds == null || parentIds.isEmpty() ? null : StringUtils.unquote(parentIds.get(0));
                    System.err.println(childIds + " added to " + parentIds);
                    System.err.println(inv.getArgument() + " added to " + inv.getTarget());
                    Node<String> n = new NodeImpl<>(cid, null, (int)inv.getStart(), cid);
                    nodes.put(cid, n);
                    if (pid != null) {
                        Node<String> parNode = nodes.get(pid);
                        if (parNode == null) {
                            parNode = new NodeImpl<>(pid, null, (int)inv.getStart(), cid);
                            nodes.put(pid, parNode);
                        }
                        ((NodeImpl<String>)parNode).add(n);
                    }
                }
                for (Node<String> node : nodes.values()) {
                    if (((NodeImpl<String>)node).getParent() == null) {
                        result.add(node);
                    }
                }
            }
            System.out.println("result = " + result);
            if (callback != null) {
                callback.setTree(result);
            }
        }
    }
}
