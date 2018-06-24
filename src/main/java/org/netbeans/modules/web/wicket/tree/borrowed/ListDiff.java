/*
 * Decompiled with CFR 0_130.
 */
package org.netbeans.modules.web.wicket.tree.borrowed;

import java.util.List;
import org.netbeans.modules.web.wicket.tree.borrowed.Change;
import org.netbeans.modules.web.wicket.tree.borrowed.Diff;
import org.netbeans.modules.web.wicket.tree.borrowed.ParallelIterator;

final class ListDiff<T>
extends Diff<T> {
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
            this.changes = new ParallelIterator<T>(this.old, this.nue).getChanges();
        }
        return this.changes;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Old:\n");
        sb.append(this.old);
        sb.append("\nNew:\n");
        sb.append(this.nue);
        sb.append("\nChanges\n");
        sb.append(this.getChanges());
        return sb.toString();
    }
}

