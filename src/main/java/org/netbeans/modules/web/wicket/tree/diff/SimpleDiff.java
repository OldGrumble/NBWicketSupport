/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
 */
package org.netbeans.modules.web.wicket.tree.diff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Convenience class for producing Diff objects when the set of changes are
 * known to the code producing the diff, and should not be calculated from a set
 * of lists.
 *
 * @author Tim Boudreau
 */
final class SimpleDiff<T> extends Diff<T> {

    private List<T> old;
    private List<T> nue;
    private List<Change> changes;

    public SimpleDiff() {
        //do nothing
    }

    /**
     * Create a diff with initial lists.
     *
     * @param old The original list
     * @param nue The transformed list
     */
    public SimpleDiff(List<T> old, List<T> nue) {
        this.old = old;
        this.nue = nue;
    }

    public SimpleDiff(List<T> list) {
        this(list, list);
    }

    /**
     * Create a diff with initial lists and a single change
     * 
     * @param old The original list
     * @param nue The transformed list
     * @param start The index of the start position
     * @param end The index of the end position
     * @param type The type of the change
     */
    public SimpleDiff(List<T> old, List<T> nue, int start, int end, int type) {
        this(old, nue);
        add(start, end, type);
    }

    /**
     * Create a diff with no start and end lists and a single change
     *
     * @param start The index of the start position
     * @param end The index of the end position
     * @param type The type of the change
     */
    public SimpleDiff(int start, int end, int type) {
        add(start, end, type);
    }

    /**
     * Set the pre-change list's contents. This method will not copy the list.
     *
     * @param old The old contents.
     */
    public void setOld(List<T> old) {
        setOld(old, false);
    }

    /**
     * Set the post-change lists's contents. This method will not copy the list.
     *
     * @param nue The new contents.
     */
    public void setNew(List<T> nue) {
        setNew(old, false);
    }

    /**
     * Set the pre-change list's contents. If the copy argument is true, a copy
     * of the passed in list, not the original, will be saved.
     *
     * @param old The old contents.
     * @param copy A flag indicating if the list should be copied before saving.
     */
    public void setOld(List<T> old, boolean copy) {
        this.old = copy ? new ArrayList<>(old) : old;
    }

    /**
     * Set the pre-change list's contents. If the copy argument is true, a copy
     * of the passed in list, not the original, will be saved
     *
     * @param nue The new contents.
     * @param copy A flag indicating if the list should be copied before saving.
     */
    public void setNew(List<T> nue, boolean copy) {
        this.nue = copy ? new ArrayList<>(nue) : nue;
    }

    /**
     * Add a change to this diff.
     *
     * @param start The index of the start position
     * @param end The index of the end position
     * @param type The type of the change
     */
    public void add(int start, int end, int type) {
        changes.add(new C(start, end, type));
    }

    /**
     * Add a change to this diff.
     *
     * @param change The change
     */
    public void add(Change change) {
        changes.add(new C(change));
    }

    @Override
    public List<Change> getChanges() {
        return Collections.<Change>unmodifiableList(changes);
    }

    @Override
    public List<T> getOld() {
        return old;
    }

    @Override
    public List<T> getNew() {
        return nue;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Iterator i = changes.iterator(); i.hasNext();) {
            Change change = (Change)i.next();
            sb.append(change);
            if (i.hasNext()) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    static final class C implements Change {

        private final int start;
        private final int end;
        private final int type;

        C(int start, int end, int type) {
            this.start = start;
            this.end = end;
            this.type = type;
            assert start <= end : "Start must be <= than end. Start: "
                    + start + " end " + end;
            assert type == CHANGE || type == DELETE || type == INSERT :
                    "Unknown change type: " + type;
        }

        C(Change change) {
            this.start = change.getStart();
            this.end = change.getEnd();
            this.type = change.getType();
        }

        @Override
        public int getStart() {
            return start;
        }

        @Override
        public int getEnd() {
            return end;
        }

        @Override
        public int getType() {
            return type;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Change) {
                Change c = (Change)o;
                return c.getStart() == getStart() && c.getEnd() == getEnd() && c.getType() == getType();
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return (start + end) * ((type + 3) * 1299709);
        }

        @Override
        public final String toString() {
            StringBuilder sb = new StringBuilder();
            switch (type) {
                case INSERT:
                    sb.append("INSERT "); //NOI18N
                    break;
                case DELETE:
                    sb.append("DELETE "); //NOI18N
                    break;
                case CHANGE:
                    sb.append("CHANGE "); //NOI18N
                    break;
                default:
                    assert false;
            }
            sb.append(start);
            sb.append('-'); //NOI18N
            sb.append(end);
            return sb.toString();
        }
    }
}
