/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.tree;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Tim Boudreau
 */
public abstract class Node<T> implements Comparable<Node<T>> {

    /**
     * Package-private default constructor to ensure instances cannot be created
     * from outside the package.
     */
    Node() {
    }

    public abstract String getId();

    public abstract String getPath();

    public abstract List<Node<T>> getChildren();

    public abstract int getOffset();

    public abstract T getData();

    public abstract Map<String, String> getAttributes();
}
