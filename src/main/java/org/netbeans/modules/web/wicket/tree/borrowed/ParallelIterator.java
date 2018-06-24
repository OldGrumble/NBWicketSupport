/*
 * Decompiled with CFR 0_130.
 */
package org.netbeans.modules.web.wicket.tree.borrowed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.web.wicket.tree.borrowed.Change;
import org.netbeans.modules.web.wicket.tree.borrowed.ListDiffChange;

final class ParallelIterator<T> {
    private final List<T> old;
    private final List<T> nue;
    private Iterator<T> oi;
    private Iterator<T> ni;
    private Set<T> h_old = null;
    private Set<T> h_new = null;
    private final List<Change> changes = new ArrayList<Change>(5);
    private T lastOld;
    private T lastNue;
    private boolean oiHadNext = true;
    private boolean done = false;
    private int offset = 0;
    private int index = 0;
    private ListDiffChange currChange = null;

    ParallelIterator(List<T> old, List<T> nue) {
        this.old = old;
        this.nue = nue;
        this.oi = old.iterator();
        this.ni = nue.iterator();
    }

    private void go() {
        if (this.oi == null) {
            throw new IllegalStateException("Cannot reuse");
        }
        if (this.old.isEmpty() && this.nue.isEmpty()) {
            this.oi = null;
            this.ni = null;
            return;
        }
        if (!this.old.isEmpty() && this.nue.isEmpty()) {
            ListDiffChange change = new ListDiffChange(0, 2);
            change.setEnd(this.old.size() - 1);
            this.changes.add(change);
            this.oi = null;
            this.ni = null;
            return;
        }
        if (this.old.isEmpty() && !this.nue.isEmpty()) {
            ListDiffChange change = new ListDiffChange(0, 1);
            change.setEnd(this.nue.size() - 1);
            this.changes.add(change);
            this.oi = null;
            this.ni = null;
            return;
        }
        this.ensureInit();
        while (this.hasNext()) {
            this.next();
        }
        this.done();
    }

    private boolean hasNext() {
        return !this.done && (this.oi.hasNext() || this.ni.hasNext() || this.lastOld != null || this.lastNue != null);
    }

    private void handled(T o, List<T> src) {
        if (src == this.old) {
            this.lastOld = null;
            if (this.oi.hasNext()) {
                this.lastOld = this.oi.next();
                ++this.index;
            }
        } else {
            this.lastNue = null;
            if (this.ni.hasNext()) {
                this.lastNue = this.ni.next();
            }
        }
    }

    private void next() {
        boolean match;
        boolean oiNext = this.oi.hasNext();
        boolean niNext = this.ni.hasNext();
        boolean bl = match = this.lastOld != null && this.lastNue != null && this.lastOld.equals(this.lastNue);
        if (match) {
            this.writeChange();
            this.handled(this.lastOld, this.old);
            this.handled(this.lastNue, this.nue);
        } else {
            this.ensureSets();
            boolean nueHasIt = this.h_new.contains(this.lastOld);
            boolean oldHasIt = this.h_old.contains(this.lastNue);
            if (this.lastNue == null && this.lastOld != null) {
                ListDiffChange last;
                this.writeChange();
                this.currChange = last = new ListDiffChange(this.index + this.offset, this.old.size() - 1 + this.offset, 2);
                this.done = true;
            } else if (this.lastOld == null && this.lastNue != null) {
                for (int i = this.index + 1; i < this.nue.size() - this.offset; ++i) {
                    this.addChange(1, i);
                }
                this.done = true;
            } else if (nueHasIt && !oldHasIt) {
                this.addChange(1, this.index);
                this.handled(this.lastNue, this.nue);
            } else if (!nueHasIt && oldHasIt) {
                this.addChange(2, this.index);
                this.handled(this.lastOld, this.old);
            } else if (nueHasIt && oldHasIt) {
                this.addChange(0, this.index);
                this.handled(this.lastOld, this.old);
                this.handled(this.lastNue, this.nue);
            } else if (!nueHasIt && !oldHasIt && (oiNext || !oiNext && this.oiHadNext)) {
                this.addChange(0, this.index);
                this.handled(this.lastOld, this.old);
                this.handled(this.lastNue, this.nue);
                if (!oiNext) {
                    for (int i = this.index + 1; i < this.nue.size() - this.offset; ++i) {
                        this.addChange(1, i);
                    }
                    this.done = true;
                } else if (!niNext) {
                    for (int i = this.index; i < this.old.size() - this.offset; ++i) {
                        this.addChange(2, i);
                    }
                    this.done = true;
                }
            }
        }
        this.oiHadNext = oiNext;
    }

    private void ensureSets() {
        if (this.h_old == null) {
            this.h_old = new HashSet<T>(this.old);
            this.h_new = new HashSet<T>(this.nue);
            if (this.h_old.size() != this.old.size()) {
                throw new IllegalStateException("Duplicate elements - size of list does not match size of equivalent HashSet " + this.identifyDuplicates(this.old));
            }
            if (this.h_new.size() != this.nue.size()) {
                throw new IllegalStateException("Duplicate elements - size of list does not match size of equivalent HashSet " + this.identifyDuplicates(this.nue));
            }
        }
    }

    private String identifyDuplicates(List<T> l) {
        HashMap<T, Integer> map = new HashMap<T, Integer>();
        for (T o : l) {
            Integer count = (Integer)map.get(o);
            count = count == null ? new Integer(1) : new Integer(count + 1);
            map.put(o, count);
        }
        StringBuffer sb = new StringBuffer("Duplicates: ");
        for (Object key : map.keySet()) {
            Integer ct = (Integer)map.get(key);
            if (ct <= 1) continue;
            sb.append("[" + ct + " occurances of " + key + "]");
        }
        return sb.toString();
    }

    private void ensureInit() {
        if (this.lastOld == null) {
            this.lastOld = this.oi.next();
        }
        if (this.lastNue == null) {
            this.lastNue = this.ni.next();
        }
    }

    private void done() {
        this.writeChange();
        this.currChange = null;
        this.oi = null;
        this.ni = null;
        this.h_old = null;
        this.h_new = null;
    }

    List<Change> getChanges() {
        if (this.oi != null) {
            this.go();
        }
        return this.changes;
    }

    private void addChange(int type, int idx) {
        if (this.currChange == null) {
            this.currChange = new ListDiffChange(idx + this.offset, type);
        } else if (this.currChange.getType() == type) {
            this.currChange.inc();
        } else {
            this.writeChange();
            this.currChange = new ListDiffChange(idx + this.offset, type);
        }
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
        assert (this.currChange.getStart() <= this.currChange.getEnd());
        this.currChange = null;
    }
}

