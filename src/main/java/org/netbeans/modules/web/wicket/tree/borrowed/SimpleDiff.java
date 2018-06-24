/*
 * Decompiled with CFR 0_130.
 */
package org.netbeans.modules.web.wicket.tree.borrowed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.web.wicket.tree.borrowed.Change;
import org.netbeans.modules.web.wicket.tree.borrowed.Diff;

final class SimpleDiff<T>
extends Diff<T> {
    private List<T> old;
    private List<T> nue;
    private List<Change> changes;

    public SimpleDiff() {
    }

    public SimpleDiff(List<T> old, List<T> nue) {
        this.old = old;
        this.nue = nue;
    }

    public SimpleDiff(List<T> list) {
        this(list, list);
    }

    public SimpleDiff(List<T> old, List<T> nue, int start, int end, int type) {
        this(old, nue);
        this.add(start, end, type);
    }

    public SimpleDiff(int start, int end, int type) {
        this.add(start, end, type);
    }

    public void setOld(List<T> old) {
        this.setOld(old, false);
    }

    public void setNew(List<T> nue) {
        this.setNew(this.old, false);
    }

    public void setOld(List<T> old, boolean copy) {
        this.old = copy ? new ArrayList(old) : old;
    }

    public void setNew(List<T> nue, boolean copy) {
        this.nue = copy ? new ArrayList(nue) : nue;
    }

    public void add(int start, int end, int type) {
        this.changes.add(new C(start, end, type));
    }

    public void add(Change change) {
        this.changes.add(new C(change));
    }

    @Override
    public List<Change> getChanges() {
        return Collections.unmodifiableList(this.changes);
    }

    @Override
    public List<T> getOld() {
        return this.old;
    }

    @Override
    public List<T> getNew() {
        return this.nue;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        Iterator<Change> i = this.changes.iterator();
        while (i.hasNext()) {
            Change change = i.next();
            sb.append(change);
            if (!i.hasNext()) continue;
            sb.append(",");
        }
        return sb.toString();
    }

    static final class C
    implements Change {
        private final int start;
        private final int end;
        private final int type;

        C(int start, int end, int type) {
            this.start = start;
            this.end = end;
            this.type = type;
            assert (start <= end);
            assert (type == 0 || type == 2 || type == 1);
        }

        C(Change change) {
            this.start = change.getStart();
            this.end = change.getEnd();
            this.type = change.getType();
        }

        @Override
        public int getStart() {
            return this.start;
        }

        @Override
        public int getEnd() {
            return this.end;
        }

        @Override
        public int getType() {
            return this.type;
        }

        public boolean equals(Object o) {
            if (o instanceof Change) {
                Change c = (Change)o;
                return c.getStart() == this.getStart() && c.getEnd() == this.getEnd() && c.getType() == this.getType();
            }
            return false;
        }

        public int hashCode() {
            return (this.start + this.end) * ((this.type + 3) * 1299709);
        }

        public final String toString() {
            StringBuffer sb = new StringBuffer();
            switch (this.type) {
                case 1: {
                    sb.append("INSERT ");
                    break;
                }
                case 2: {
                    sb.append("DELETE ");
                    break;
                }
                case 0: {
                    sb.append("CHANGE ");
                    break;
                }
                default: {
                    assert (false);
                    break;
                }
            }
            sb.append(this.start);
            sb.append('-');
            sb.append(this.end);
            return sb.toString();
        }
    }

}

