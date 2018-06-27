/*
 * Some license issues have still to be clarified, especially for the "borrowed"
 * package, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.tree.borrowed;

final class ListDiffChange implements Change {

    private final int type;
    private final int start;
    private int end;

    public ListDiffChange(int start, int end, int type) {
        this.type = type;
        this.start = start;
        this.end = end;
        if (end < start) {
            throw new IllegalArgumentException("Start " + start + " > " + end);
        }
        if (end < 0 || start < 0) {
            throw new IllegalArgumentException("Negative start " + start + " or end " + end);
        }
        if (type != 2 && type != 0 && type != 1) {
            throw new IllegalArgumentException("Unknown change type " + type);
        }
    }

    ListDiffChange(int start, int type) {
        this.start = start;
        this.end = start;
        this.type = type;
        assert (type == 2 || type == 0 || type == 1);
    }

    void inc() {
        ++this.end;
    }

    void setEnd(int end) {
        assert (end >= this.start);
        this.end = end;
    }

    @Override
    public final int getType() {
        return this.type;
    }

    @Override
    public final int getStart() {
        return this.start;
    }

    @Override
    public final int getEnd() {
        return this.end;
    }

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
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
