/*
 * Some license issues have still to be clarified, especially for the "borrowed"
 * package, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.tree.borrowed;

import java.util.List;

/**
 *
 *
 * @author Peter Nabbefeld
 * @author Geertjan Wielenga (original author)
 *
 * @param <T>
 */
final class ListDiff<T> extends Diff<T> {

    private final List<T> old;
    private final List<T> nue;
    List<Change> changes = null;

    ListDiff(List<T> old, List<T> nue) {
        this.old = old;
        this.nue = nue;
    }

    @Override
    public List<T> getOld() {
        return this.old;
    }

    @Override
    public List<T> getNew() {
        return this.nue;
    }

    @Override
    public List<Change> getChanges() {
        if (this.changes == null) {
            this.changes = new ParallelIterator<>(this.old, this.nue).getChanges();
        }
        return this.changes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Old:\n");
        sb.append(this.old);
        sb.append("\nNew:\n");
        sb.append(this.nue);
        sb.append("\nChanges\n");
        sb.append(this.getChanges());
        return sb.toString();
    }
}
