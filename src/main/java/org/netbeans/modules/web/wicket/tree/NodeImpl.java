/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.web.wicket.tree;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author peter
 */
final class NodeImpl<T> extends Node<T> {

    private NodeImpl<T> parent;
    private final String id;
    private final int offset;
    private List<Node<T>> children;
    private T data;
    private Map<String, String> attributes;

    NodeImpl(String id, NodeImpl<T> parent, int offset) {
        this(id, parent, offset, null);
    }

    NodeImpl(String id, NodeImpl<T> parent, int offset, T data) {
        this.id = id;
        this.offset = offset;
        this.parent = parent;
        this.data = data;
    }

    Node<T> getParent() {
        NodeImpl<T> result = this.parent;
        if (result != null && "".equals(result.getId())) {
            result = null;
        }
        return result;
    }

    void add(Node<T> n) {
        if (this.children == null) {
            this.children = new LinkedList<>();
        }
        ((NodeImpl)n).parent = this;
        this.children.add(n);
    }

    void setAttributesMap(Map<String, String> keyValuePairs) {
        this.attributes = keyValuePairs;
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
        NodeImpl<T> par = this.parent;
        while (par != null) {
            sb.insert(0, par.getId());
            sb.insert(0, '/');
            par = par.parent;
        }
        return sb.toString();
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

    @Override
    public String toString() {
        return this.getId();
    }

    @Override
    public boolean equals(Object o) {
        boolean result;
        boolean bl = result = o != null && o.getClass() == NodeImpl.class;
        if (result) {
            result &= ((NodeImpl)o).getId().equals(this.getId());
        }
        return result;
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
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
