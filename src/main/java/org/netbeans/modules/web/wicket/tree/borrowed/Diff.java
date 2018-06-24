/*
 * Decompiled with CFR 0_130.
 */
package org.netbeans.modules.web.wicket.tree.borrowed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.web.wicket.tree.borrowed.Change;
import org.netbeans.modules.web.wicket.tree.borrowed.ListDiff;
import org.netbeans.modules.web.wicket.tree.borrowed.ListMatcherAdapter;
import org.netbeans.modules.web.wicket.tree.borrowed.Measure;

public abstract class Diff<T> {

    private static boolean VALIDATE = Boolean.getBoolean("org.netbeans.misc.diff.validate");
    private static boolean $assertionsDisabled;

    public static <T> Diff<T> create(List<T> old, List<T> nue) {
        ListDiff<T> result = new ListDiff<T>(old, nue);
        if (VALIDATE && !$assertionsDisabled && !Diff.validDiff(result)) {
            throw new AssertionError();
        }
        return result;
    }

    public static <T> Diff<T> create(List<T> old, List<T> nue, Algorithm algorithm) {
        Diff<T> result;
        switch (algorithm) {
            case ITERATIVE: {
                result = Diff.create(old, nue);
                break;
            }
            case LONGEST_COMMON_SEQUENCE: {
                result = new ListMatcherAdapter<T>(old, nue);
                break;
            }
            default: {
                throw new AssertionError();
            }
        }
        if (VALIDATE && !Diff.validDiff(result)) {
            throw new IllegalStateException("Invalid diff " + result);
        }
        return result;
    }

    public static <T> Diff<T> create(List<T> old, List<T> nue, Measure measure) {
        ListMatcherAdapter<T> result = new ListMatcherAdapter<T>(old, nue, measure);
        if (VALIDATE && !Diff.validDiff(result)) {
            throw new IllegalStateException("Invalid diff " + result);
        }
        return result;
    }

    public static <T> Diff<T> createPredefined(List<T> old, List<T> nue, List<Change> changes) {
        if (old == null) {
            throw new NullPointerException("Old list null");
        }
        if (nue == null) {
            throw new NullPointerException("New list null");
        }
        if (changes == null) {
            throw new NullPointerException("Change list null");
        }
        ListDiff<T> result = new ListDiff<T>(old, nue);
        result.changes = changes;
        if (!Diff.validDiff(result)) {
            throw new IllegalStateException("Invalid diff " + result);
        }
        return result;
    }

    private static final <T> boolean validDiff(Diff<T> diff) {
        boolean result;
        ArrayList<T> list = new ArrayList<T>(diff.getOld());
        List<T> target = diff.getNew();
        List<Change> changes = diff.getChanges();
        block5:
        for (Change change : changes) {
            int start = change.getStart();
            int end = change.getEnd();
            switch (change.getType()) {
                case 0: {
                    for (int i = start; i <= end; ++i) {
                        list.set(i, target.get(i));
                    }
                    continue block5;
                }
                case 1: {
                    for (int i = end; i >= start; --i) {
                        T o = target.get(i);
                        list.add(start, o);
                    }
                    continue block5;
                }
                case 2: {
                    for (int i = end; i >= start; --i) {
                        list.remove(i);
                    }
                    break;
                }
            }
        }
        int max = target.size();
        boolean bl = result = max == list.size();
        if (result) {
            for (int i = 0; i < max; ++i) {
                if ((result &= target.get(i) == null == (list.get(i) == null)) && target.get(i) != null) {
                    result &= ((String)target.get(i)).equals(list.get(i));
                }
                if (!result) {
                    break;
                }
            }
        }
        return result;
    }

    public static enum Algorithm {
        ITERATIVE,
        LONGEST_COMMON_SEQUENCE;

        private Algorithm() {
        }
    }

    public abstract List<Change> getChanges();

    public abstract List<T> getOld();

    public abstract List<T> getNew();
}
