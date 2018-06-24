/*
 * Decompiled with CFR 0_130.
 * 
 * Could not load the following classes:
 *  org.openide.util.NbBundle
 */
package org.netbeans.modules.web.wicket.tree;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.web.wicket.tree.MarkupContainerTree;
import org.netbeans.modules.web.wicket.tree.borrowed.Change;
import org.netbeans.modules.web.wicket.tree.borrowed.Diff;
import org.openide.util.NbBundle;

public final class TreeDiff {

    private MarkupContainerTree<String> java;
    private MarkupContainerTree<String> html;
    private static final Logger ERR = Logger.getLogger(TreeDiff.class.getPackage().getName());

    public TreeDiff(MarkupContainerTree<String> html, MarkupContainerTree<String> java) {
        this.html = html;
        this.java = java;
    }

    public List<Problem> getProblems() {
        ArrayList<Problem> problems = new ArrayList<Problem>();
        this.diff(this.html.getRoot(), this.java.getRoot(), problems);
        return problems;
    }

    private void diff(MarkupContainerTree.Node<String> htmlNode, MarkupContainerTree.Node<String> javaNode, List<Problem> problems) {
        boolean javaDups;
        List<MarkupContainerTree.Node<String>> dups;
        Problem p;
        List<String> htmlKids = this.getChildNodesIds(htmlNode);
        List<String> javaKids = this.getChildNodesIds(javaNode);
        Iterator<MarkupContainerTree.Node<String>> i1 = htmlNode.getChildren().iterator();
        Iterator<MarkupContainerTree.Node<String>> i2 = javaNode.getChildren().iterator();
        while (i1.hasNext() && i2.hasNext()) {
            MarkupContainerTree.Node<String> n1 = i1.next();
            MarkupContainerTree.Node<String> n2 = i2.next();
            if (!n1.getId().equals(n2.getId())) {
                continue;
            }
            this.diff(n1, n2, problems);
        }
        int htmlUniques = new HashSet<String>(htmlKids).size();
        int javaUniques = new HashSet<String>(javaKids).size();
        boolean htmlDups = htmlUniques != htmlKids.size();
        boolean bl = javaDups = javaUniques != javaKids.size();
        if (htmlDups) {
            dups = this.findDuplicateIds(htmlNode);
            for (MarkupContainerTree.Node<String> dup : dups) {
                p = new Problem(ProblemKind.DUPLICATE_HTML_IDS, null, dup, null, htmlNode);
                problems.add(p);
            }
        }
        if (javaDups) {
            dups = this.findDuplicateIds(javaNode);
            for (MarkupContainerTree.Node<String> dup : dups) {
                p = new Problem(ProblemKind.DUPLICATE_JAVA_IDS, dup, null, javaNode, null);
                problems.add(p);
            }
        }
        if (htmlDups || javaDups) {
            return;
        }
        Diff<String> diff = Diff.create(htmlKids, javaKids, Diff.Algorithm.LONGEST_COMMON_SEQUENCE);
        List<Change> changes = diff.getChanges();
        System.err.println("CHANGES: " + changes);
        block8:
        for (Change c : changes) {
            int start = c.getStart();
            int end = c.getEnd();
            Problem p2;
            switch (c.getType()) {
                case 1: {
                    for (int i = start; i <= end; ++i) {
                        p2 = new Problem(ProblemKind.HTML_NODE_MISSING, javaNode.getChildren().get(i), null, javaNode, htmlNode);
                        problems.add(p2);
                        p2 = new Problem(ProblemKind.JAVA_NODE_ADDED, javaNode.getChildren().get(i), null, javaNode, htmlNode);
                        problems.add(p2);
                    }
                    continue block8;
                }
                case 2: {
                    for (int i = start; i <= end; ++i) {
                        p2 = new Problem(ProblemKind.JAVA_NODE_MISSING, null, htmlNode.getChildren().get(i), javaNode, htmlNode);
                        problems.add(p2);
                        p2 = new Problem(ProblemKind.HTML_NODE_ADDED, null, htmlNode.getChildren().get(i), javaNode, htmlNode);
                        problems.add(p2);
                    }
                    continue block8;
                }
                case 0: {
                    for (int i = start; i <= end; ++i) {
                        p2 = new Problem(ProblemKind.DIFFERENT_IDS, javaNode.getChildren().get(i), htmlNode.getChildren().get(i), javaNode, htmlNode);
                        problems.add(p2);
                    }
                    break;
                }
            }
        }
    }

    private List<MarkupContainerTree.Node<String>> findDuplicateIds(MarkupContainerTree.Node<String> parent) {
        HashSet<String> dupCheck = new HashSet<String>();
        ArrayList<MarkupContainerTree.Node<String>> result = new ArrayList<MarkupContainerTree.Node<String>>();
        for (MarkupContainerTree.Node<String> child : parent.getChildren()) {
            if (dupCheck.contains(child.getId())) {
                result.add(child);
            }
            dupCheck.add(child.getId());
        }
        return result;
    }

    private List<String> getChildNodesIds(MarkupContainerTree.Node<String> n) {
        ArrayList<String> result = new ArrayList<String>(n.getChildren().size());
        ArrayList<MarkupContainerTree.Node<String>> kids = new ArrayList<MarkupContainerTree.Node<String>>(n.getChildren());
        Collections.sort(kids);
        for (MarkupContainerTree.Node<String> child : kids) {
            result.add(child.getId());
        }
        return result;
    }

    public static final class Problem {

        private final MarkupContainerTree.Node<String> problemJavaNode;
        private final MarkupContainerTree.Node<String> problemHtmlNode;
        private final MarkupContainerTree.Node<String> problemParentJavaNode;
        private final MarkupContainerTree.Node<String> problemParentHtmlNode;
        private final ProblemKind kind;

        public Problem(ProblemKind kind, MarkupContainerTree.Node<String> problemJavaNode, MarkupContainerTree.Node<String> problemHtmlNode, MarkupContainerTree.Node<String> problemParentJavaNode, MarkupContainerTree.Node<String> problemParentHtmlNode) {
            this.problemJavaNode = problemJavaNode;
            this.problemHtmlNode = problemHtmlNode;
            this.problemParentJavaNode = problemParentJavaNode;
            this.problemParentHtmlNode = problemParentHtmlNode;
            this.kind = kind;
            System.err.println("CREATE PROBLEM " + this);
        }

        public ProblemKind getKind() {
            return this.kind;
        }

        public MarkupContainerTree.Node<String> getProblemHtmlNode() {
            return this.problemHtmlNode;
        }

        public MarkupContainerTree.Node<String> getProblemJavaNode() {
            return this.problemJavaNode;
        }

        public MarkupContainerTree.Node<String> getProblemParentHtmlNode() {
            return this.problemParentHtmlNode;
        }

        public MarkupContainerTree.Node<String> getProblemParentJavaNode() {
            return this.problemParentJavaNode;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            switch (this.getKind()) {
                case DIFFERENT_IDS: {
                    String problemParentId = this.problemParentHtmlNode.getId();
                    String javaChildId = this.getProblemJavaNode().getId();
                    String htmlChildId = this.getProblemHtmlNode().getId();
                    sb.append(NbBundle.getMessage(Problem.class, (String)this.kind.name(), (Object)problemParentId, (Object)htmlChildId, (Object)javaChildId));
                    break;
                }
                case DUPLICATE_HTML_IDS: {
                    String problemParentId = this.getProblemParentHtmlNode().getId();
                    String problemChildId = this.getProblemHtmlNode().getId();
                    sb.append(NbBundle.getMessage(Problem.class, (String)this.kind.name(), (Object)problemParentId, (Object)problemChildId));
                    break;
                }
                case DUPLICATE_JAVA_IDS: {
                    String problemParentId = this.getProblemParentJavaNode().getId();
                    String problemChildId = this.getProblemJavaNode().getId();
                    sb.append(NbBundle.getMessage(Problem.class, (String)this.kind.name(), (Object)problemParentId, (Object)problemChildId));
                    break;
                }
                case HTML_NODE_ADDED: {
                    String problemParentId = this.getProblemParentHtmlNode().getId();
                    String problemChildId = this.getProblemHtmlNode().getId();
                    sb.append(NbBundle.getMessage(Problem.class, (String)this.kind.name(), (Object)problemParentId, (Object)problemChildId));
                    break;
                }
                case JAVA_NODE_ADDED: {
                    String problemParentId = this.getProblemParentJavaNode().getId();
                    String problemChildId = this.getProblemJavaNode().getId();
                    sb.append(NbBundle.getMessage(Problem.class, (String)this.kind.name(), (Object)problemParentId, (Object)problemChildId));
                    break;
                }
                case JAVA_NODE_MISSING: {
                    String problemParentId = this.getProblemParentJavaNode().getId();
                    String problemChildId = this.getProblemHtmlNode().getId();
                    sb.append(NbBundle.getMessage(Problem.class, (String)this.kind.name(), (Object)problemParentId, (Object)problemChildId));
                    break;
                }
                case HTML_NODE_MISSING: {
                    String problemParentId = this.getProblemParentHtmlNode().getId();
                    String problemChildId = this.getProblemJavaNode().getId();
                    sb.append(NbBundle.getMessage(Problem.class, (String)this.kind.name(), (Object)problemParentId, (Object)problemChildId));
                    break;
                }
                default: {
                    throw new AssertionError((Object)this.getKind().toString());
                }
            }
            if (ERR.isLoggable(Level.FINE)) {
                sb.append("\n  problemJavaNode " + this.problemJavaNode);
                sb.append("\n  problemJavaParent " + this.problemParentJavaNode);
                sb.append("\n  problemHtmlNode " + this.problemHtmlNode);
                sb.append("\n  problemHtmlParent " + this.problemParentHtmlNode);
                sb.append(")");
            }
            return sb.toString();
        }
    }

    public static enum ProblemKind {
        HTML_NODE_MISSING,
        JAVA_NODE_MISSING,
        DUPLICATE_HTML_IDS,
        DUPLICATE_JAVA_IDS,
        HTML_NODE_ADDED,
        JAVA_NODE_ADDED,
        DIFFERENT_IDS;

        private ProblemKind() {
        }

        public boolean isHtmlProblem() {
            switch (this) {
                case DIFFERENT_IDS: {
                    return true;
                }
                case JAVA_NODE_ADDED:
                case JAVA_NODE_MISSING:
                case DUPLICATE_JAVA_IDS: {
                    return false;
                }
            }
            return true;
        }

        public boolean isJavaProblem() {
            if (this == DIFFERENT_IDS) {
                return true;
            }
            return !this.isHtmlProblem();
        }
    }

}
