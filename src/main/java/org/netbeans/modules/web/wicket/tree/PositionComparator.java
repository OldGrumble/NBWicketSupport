/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.tree;

import java.util.Comparator;

/**
 *
 * @author Tim Boudreau
 */
public final class PositionComparator implements Comparator<Node> {

    @Override
    public int compare(Node a, Node b) {
        return a.getOffset() - b.getOffset();
    }
}
