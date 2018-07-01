/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

/**
 *
 * @author Tim Boudreau
 */
public final class MarkupContainerTree<T> {

    private final NodeImpl<T> root;

    public MarkupContainerTree() {
        this.root = new NodeImpl("root", null, 0);
    }

    public MarkupContainerTree(Node<T> root) {
        this.root = (NodeImpl<T>)root;
    }

    public Node<T> getRoot() {
        return this.root;
    }

    public static <T> MarkupContainerTree<T> merge(MarkupContainerTree<T> a, MarkupContainerTree<T> b) {
        MarkupContainerTree<T> result = new MarkupContainerTree<>();
        Node<T> aRoot = a.getRoot();
        Node<T> bRoot = b.getRoot();
        MarkupContainerTree.merge(aRoot, bRoot, result.getRoot());
        return result;
    }

    private static <T> void merge(Node<T> a, Node<T> b, Node<T> parent) {
        String id;
        assert (a.getId().equals(b.getId()));
        NodeImpl<T> result = new NodeImpl<>(a.getId(), (NodeImpl)parent, a.getOffset(), a.getData());
        HashMap<String, Node<T>> aNodes = new HashMap<>();
        HashMap<String, Node<T>> bNodes = new HashMap<>();
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
                NodeImpl<T> nue = new NodeImpl(s, result, aa.getOffset(), aa.getData());
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
        ArrayList<Node<T>> l = new ArrayList<>(root.getChildren());
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

    void add(Node<T> n) {
        this.root.add(n);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(super.toString());
        b.append('\n');
        this.outNode(this.root, 0, b);
        return b.toString();
    }

    private void outNode(NodeImpl<T> n, int depth, StringBuilder b) {
        char[] c = new char[depth * 3];
        Arrays.fill(c, ' ');
        b.append(c);
        b.append(n.getId()).append(" (").append(n.getData()).append(")");
        b.append('\n');
        for (Node<T> nn : n.getChildren()) {
            this.outNode((NodeImpl)nn, depth + 1, b);
        }
    }

    Node<T> findNode(String id) {
        if (id.equals(this.root.getId())) {
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
}
