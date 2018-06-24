/*
 * Decompiled with CFR 0_130.
 */
package org.netbeans.modules.web.wicket.tree.borrowed;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.web.wicket.tree.borrowed.Change;
import org.netbeans.modules.web.wicket.tree.borrowed.Diff;
import org.netbeans.modules.web.wicket.tree.borrowed.ListDiffChange;
import org.netbeans.modules.web.wicket.tree.borrowed.ListMatcher;
import org.netbeans.modules.web.wicket.tree.borrowed.Measure;

class ListMatcherAdapter<E> extends Diff<E> {

    final ListMatcher m;
    boolean unmatched = true;
    List<E> old;
    List<E> nue;

    public ListMatcherAdapter(List<E> old, List<E> nue) {
        this.m = ListMatcher.instance(old, nue);
        this.old = old;
        this.nue = nue;
    }

    public ListMatcherAdapter(List<E> old, List<E> nue, Measure measure) {
        this.m = ListMatcher.instance(old, nue, measure);
        this.old = old;
        this.nue = nue;
    }

    @Override
    public List getChanges() {
        if (this.unmatched) {
            this.m.match();
            this.unmatched = false;
        }
        ListMatcher.ResultItem r[] = m.getTransformedResult();
        return (new IndexProcessor(r)).changes;
    }

    @Override
    public List getOld() {
        return this.old;
    }

    @Override
    public List getNew() {
        return this.nue;
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

    private class IndexProcessor /* Umbenannt; Originalname = "I" */ {

        public final List<Change> changes = new ArrayList<Change>();
        ListDiffChange currChange = null;
        int offset = 0;

        public IndexProcessor(ListMatcher.ResultItem[] items) {
            for (int i = 0; i < items.length; ++i) {
                int idx = i;
                this.proc(items[i], idx);
            }
            this.done();
        }

        private void proc(ListMatcher.ResultItem i, int idx) {
            int type = i.getChangeType();
            this.addChange(type, idx);
        }

        private void addChange(int type, int idx) {
            if (this.currChange == null) {
                if (type != -1) {
                    this.currChange = new ListDiffChange(idx + this.offset, type);
                }
            } else if (this.currChange.getType() == type) {
                this.currChange.inc();
            } else {
                this.writeChange();
                this.currChange = type == -1 ? null : new ListDiffChange(idx + this.offset, type);
            }
        }

        private void done() {
            this.writeChange();
        }

        private void writeChange() {
            if (this.currChange == null) {
                return;
            }
            this.changes.add(this.currChange);
            int type = this.currChange.getType();
            if (type == 1) {
                this.offset += this.currChange.getEnd() - this.currChange.getStart() + 1;
            } else if (type == 2) {
                this.offset -= this.currChange.getEnd() - this.currChange.getStart() + 1;
            }
            assert (this.currChange.getStart() <= this.currChange.getEnd()) : "Start must be <= end - " + currChange.getStart() + " > " + currChange.getEnd();
            this.currChange = null;
        }
    }

}
