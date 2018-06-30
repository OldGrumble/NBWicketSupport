/*
 * Decompiled with CFR 0_130.
 * 
 * Could not load the following classes:
 *  org.openide.util.NbBundle
 */
package org.netbeans.modules.web.wicket.tree.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.web.wicket.tree.MarkupContainerTree;
import org.netbeans.modules.web.wicket.tree.Node;
import org.netbeans.modules.web.wicket.tree.diff.Change;
import org.netbeans.modules.web.wicket.tree.diff.Diff;
import org.openide.util.NbBundle;

public final class TreeDiff {

    private final MarkupContainerTree<String> java;
    private final MarkupContainerTree<String> html;
    private static final Logger ERR = Logger.getLogger(TreeDiff.class.getPackage().getName());

    public TreeDiff(MarkupContainerTree<String> html, MarkupContainerTree<String> java) {
        this.html = html;
        this.java = java;
    }

    public List<Problem> getProblems() {
        ArrayList<Problem> problems = new ArrayList<>();
        this.diff(this.html.getRoot(), this.java.getRoot(), problems);
        return problems;
    }

    private void diff(Node<String> htmlNode, Node<String> javaNode, List<Problem> problems) {
        boolean javaDups;
        List<Node<String>> dups;
        Problem p;
        List<String> htmlKids = this.getChildNodesIds(htmlNode);
        List<String> javaKids = this.getChildNodesIds(javaNode);
        Iterator<Node<String>> i1 = htmlNode.getChildren().iterator();
        Iterator<Node<String>> i2 = javaNode.getChildren().iterator();
        while (i1.hasNext() && i2.hasNext()) {
            Node<String> n1 = i1.next();
            Node<String> n2 = i2.next();
            if (!n1.getId().equals(n2.getId())) {
                continue;
            }
            this.diff(n1, n2, problems);
        }
        int htmlUniques = new HashSet<>(htmlKids).size();
        int javaUniques = new HashSet<>(javaKids).size();
        boolean htmlDups = htmlUniques != htmlKids.size();
        boolean bl = javaDups = javaUniques != javaKids.size();
        if (htmlDups) {
            dups = this.findDuplicateIds(htmlNode);
            for (Node<String> dup : dups) {
                p = new Problem(ProblemKind.DUPLICATE_HTML_IDS, null, dup, null, htmlNode);
                problems.add(p);
            }
        }
        if (javaDups) {
            dups = this.findDuplicateIds(javaNode);
            for (Node<String> dup : dups) {
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
                    continue;
                }
                case 2: {
                    for (int i = start; i <= end; ++i) {
                        p2 = new Problem(ProblemKind.JAVA_NODE_MISSING, null, htmlNode.getChildren().get(i), javaNode, htmlNode);
                        problems.add(p2);
                        p2 = new Problem(ProblemKind.HTML_NODE_ADDED, null, htmlNode.getChildren().get(i), javaNode, htmlNode);
                        problems.add(p2);
                    }
                    continue;
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

    private List<Node<String>> findDuplicateIds(Node<String> parent) {
        HashSet<String> dupCheck = new HashSet<>();
        ArrayList<Node<String>> result = new ArrayList<>();
        for (Node<String> child : parent.getChildren()) {
            if (dupCheck.contains(child.getId())) {
                result.add(child);
            }
            dupCheck.add(child.getId());
        }
        return result;
    }

    private List<String> getChildNodesIds(Node<String> n) {
        ArrayList<String> result = new ArrayList<>(n.getChildren().size());
        ArrayList<Node<String>> kids = new ArrayList<>(n.getChildren());
        Collections.sort(kids);
        for (Node<String> child : kids) {
            result.add(child.getId());
        }
        return result;
    }

    public static final class Problem {

        private final Node<String> problemJavaNode;
        private final Node<String> problemHtmlNode;
        private final Node<String> problemParentJavaNode;
        private final Node<String> problemParentHtmlNode;
        private final ProblemKind kind;

        public Problem(ProblemKind kind, Node<String> problemJavaNode, Node<String> problemHtmlNode, Node<String> problemParentJavaNode, Node<String> problemParentHtmlNode) {
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

        public Node<String> getProblemHtmlNode() {
            return this.problemHtmlNode;
        }

        public Node<String> getProblemJavaNode() {
            return this.problemJavaNode;
        }

        public Node<String> getProblemParentHtmlNode() {
            return this.problemParentHtmlNode;
        }

        public Node<String> getProblemParentJavaNode() {
            return this.problemParentJavaNode;
        }

        @Override
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
                sb.append("\n  problemJavaNode ").append(this.problemJavaNode)
                        .append("\n  problemJavaParent ").append(this.problemParentJavaNode)
                        .append("\n  problemHtmlNode ").append(this.problemHtmlNode)
                        .append("\n  problemHtmlParent ").append(this.problemParentHtmlNode)
                        .append(")");
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
