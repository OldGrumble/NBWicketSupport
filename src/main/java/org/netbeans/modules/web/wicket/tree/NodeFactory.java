/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.tree;

/**
 *
 * @author Tim Boudreau
 */
public class NodeFactory {

    static <T> Node<T> create(String id, NodeImpl<T> parent, int offset, T data) {
        return new NodeImpl<>(id, parent, offset, data);
    }

    static <T> Node<T> create(String id, NodeImpl<T> parent, int offset) {
        return new NodeImpl<>(id, parent, offset);
    }
}
