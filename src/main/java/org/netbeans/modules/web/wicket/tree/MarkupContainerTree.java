/*
 * Decompiled with CFR 0_130.
 */
package org.netbeans.modules.web.wicket.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

public final class MarkupContainerTree<T> {

    final N<T> root;

    MarkupContainerTree() {
        this.root = new N("root", null, 0);
    }

    MarkupContainerTree(N<T> root) {
        this.root = root;
    }

    N<T> getRoot() {
        return this.root;
    }

    public static <T> MarkupContainerTree<T> merge(MarkupContainerTree<T> a, MarkupContainerTree<T> b) {
        MarkupContainerTree<T> result = new MarkupContainerTree<T>();
        N<T> aRoot = a.getRoot();
        N<T> bRoot = b.getRoot();
        MarkupContainerTree.merge(aRoot, bRoot, result.getRoot());
        return result;
    }

    private static <T> void merge(Node<T> a, Node<T> b, Node<T> parent) {
        String id;
        assert (a.getId().equals(b.getId()));
        N<T> result = new N<T>(a.getId(), (N)parent, a.getOffset(), a.getData());
        HashMap<String, Node<T>> aNodes = new HashMap<String, Node<T>>();
        HashMap<String, Node<T>> bNodes = new HashMap<String, Node<T>>();
        for (Node<T> n : a.getChildren()) {
            id = n.getId();
            aNodes.put(id, n);
        }
        for (Node<T> n : b.getChildren()) {
            id = n.getId();
            bNodes.put(id, n);
        }
        HashSet<String> keys = new HashSet(aNodes.keySet());
        keys.addAll(bNodes.keySet());
        for (String s : keys) {
            Node aa = (Node)aNodes.get(s);
            Node bb = (Node)bNodes.get(s);
            if ((aa == null) != (bb == null)) {
                result.add(aa != null ? aa : bb);
            } else {
                N<T> nue = new N(s, result, aa.getOffset(), aa.getData());
                merge(aa, bb, ((Node)(nue)));
            }
        }
    }

    public TreeModel toTreeModel() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(this.getRoot());
        DefaultTreeModel mdl = new DefaultTreeModel(rootNode);
        mdl.setRoot(rootNode);
        this.addChildren(this.getRoot(), rootNode);
        return mdl;
    }

    private void addChildren(Node<T> root, DefaultMutableTreeNode nd) {
        ArrayList<Node<T>> l = new ArrayList<Node<T>>(root.getChildren());
        Collections.sort(l);
        for (Node<T> n : l) {
            DefaultMutableTreeNode nue = new DefaultMutableTreeNode(n);
            nd.add(nue);
            this.addChildren(n, nue);
        }
    }

    public List<Node<T>> getNodes() {
        return this.root.getChildren();
    }

    void add(N<T> n) {
        this.root.add(n);
    }

    public String toString() {
        StringBuilder b = new StringBuilder(super.toString());
        b.append('\n');
        this.outNode(this.root, 0, b);
        return b.toString();
    }

    private void outNode(N<T> n, int depth, StringBuilder b) {
        char[] c = new char[depth * 3];
        Arrays.fill(c, ' ');
        b.append(c);
        b.append(n.getId()).append(" (").append(n.getData()).append(")");
        b.append('\n');
        for (Node<T> nn : n.getChildren()) {
            this.outNode((N)nn, depth + 1, b);
        }
    }

    Node<T> findNode(String id) {
        if (id.equals(this.root.id)) {
            return this.root;
        }
        return this.findNode(id, this.root);
    }

    Node<T> findNode(String id, Node<T> parent) {
        for (Node<T> n : parent.getChildren()) {
            if (id.equals(n.getId())) {
                return n;
            }
            Node<T> result = this.findNode(id, n);
            if (result == null) {
                continue;
            }
            return result;
        }
        return null;
    }

    static final class N<T>
            implements Node<T> {

        private N<T> parent;
        private final String id;
        private final int offset;
        private List<Node<T>> children;
        private T data;
        private Map<String, String> attributes;

        N(String id, N<T> parent, int offset) {
            this(id, parent, offset, null);
        }

        N(String id, N<T> parent, int offset, T data) {
            this.id = id;
            this.offset = offset;
            this.parent = parent;
            this.data = data;
        }

        @Override
        public T getData() {
            return this.data;
        }

        @Override
        public String getId() {
            return this.id;
        }

        @Override
        public String getPath() {
            StringBuilder sb = new StringBuilder(this.getId());
            sb.insert(0, '/');
            N<T> par = this.parent;
            while (par != null) {
                sb.insert(0, par.getId());
                sb.insert(0, '/');
                par = par.parent;
            }
            return sb.toString();
        }

        public Node<T> getParent() {
            N<T> result = this.parent;
            if (result != null && "".equals(result.getId())) {
                result = null;
            }
            return result;
        }

        @Override
        public List<Node<T>> getChildren() {
            if (this.children == null) {
                return Collections.emptyList();
            }
            Collections.sort(this.children);
            return this.children;
        }

        @Override
        public int getOffset() {
            return this.offset;
        }

        void add(Node<T> n) {
            if (this.children == null) {
                this.children = new LinkedList<Node<T>>();
            }
            ((N)n).parent = this;
            this.children.add(n);
        }

        public String toString() {
            return this.getId();
        }

        public boolean equals(Object o) {
            boolean result;
            boolean bl = result = o != null && o.getClass() == N.class;
            if (result) {
                result &= ((N)o).getId().equals(this.getId());
            }
            return result;
        }

        public int hashCode() {
            return this.getId().hashCode();
        }

        void setAttributesMap(Map<String, String> keyValuePairs) {
            this.attributes = keyValuePairs;
        }

        @Override
        public Map<String, String> getAttributes() {
            return this.attributes;
        }

        @Override
        public int compareTo(Node o) {
            return this.getId().compareToIgnoreCase(o.getId());
        }
    }

    public static interface Node<T>
            extends Comparable<Node<T>> {

        public String getId();

        public String getPath();

        public List<Node<T>> getChildren();

        public int getOffset();

        public T getData();

        public Map<String, String> getAttributes();
    }

    public static final class PositionComparator
            implements Comparator<Node> {

        @Override
        public int compare(Node a, Node b) {
            return a.getOffset() - b.getOffset();
        }
    }

    public static final class WicketIdComparator
            implements Comparator<Node> {

        @Override
        public int compare(Node a, Node b) {
            return a.getId().compareTo(b.getId());
        }
    }

}
