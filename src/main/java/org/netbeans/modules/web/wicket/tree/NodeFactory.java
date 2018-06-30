/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.web.wicket.tree;

/**
 *
 * @author peter
 */
public class NodeFactory {

    static <T> Node<T> create(String id, NodeImpl<T> parent, int offset, T data) {
        return new NodeImpl<>(id, parent, offset, data);
    }

    static <T> Node<T> create(String id, NodeImpl<T> parent, int offset) {
        return new NodeImpl<>(id, parent, offset);
    }
}
