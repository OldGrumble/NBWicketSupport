/*
 * Decompiled with CFR 0_130.
 */
package org.netbeans.modules.web.wicket.tree.borrowed;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.netbeans.modules.web.wicket.tree.borrowed.Measure;

public final class ListMatcher<E> {

    private final E[] oldL;
    private final E[] newL;
    private final Stack<ResultItem<E>> result;
    private final Measure measure;

    private ListMatcher(List<? extends E> oldL, List<? extends E> newL, Measure measure) {
        this((E[])oldL.toArray(), (E[])newL.toArray(), measure);
    }

    private ListMatcher(List<? extends E> oldL, List<? extends E> newL) {
        this((E[])oldL.toArray(), (E[])newL.toArray());
    }

    private ListMatcher(E[] oldL, E[] newL) {
        this(oldL, newL, null);
    }

    private ListMatcher(E[] oldL, E[] newL, Measure measure) {
        this.oldL = oldL;
        this.newL = newL;
        this.measure = measure != null ? measure : Measure.DEFAULT;
        this.result = new Stack();
    }

    public static <T> ListMatcher<T> instance(List<? extends T> oldL, List<? extends T> newL) {
        return new ListMatcher<T>(oldL, newL);
    }

    public static <T> ListMatcher<T> instance(List<? extends T> oldL, List<? extends T> newL, Measure measure) {
        return new ListMatcher<T>(oldL, newL, measure);
    }

    public static <T> ListMatcher<T> instance(T[] oldL, T[] newL) {
        return new ListMatcher<T>(oldL, newL);
    }

    public boolean match() {
        int jj;
        int ii;
        boolean NEITHER = false;
        boolean UP = true;
        int LEFT = 2;
        int UP_AND_LEFT = 3;
        int UP_AND_LEFT_MOD = 4;
        int n = this.oldL.length;
        int m = this.newL.length;
        int[][] S = new int[n + 1][m + 1];
        int[][] R = new int[n + 1][m + 1];
        for (ii = 0; ii <= n; ++ii) {
            S[ii][0] = 0;
            R[ii][0] = 1;
        }
        for (jj = 0; jj <= m; ++jj) {
            S[0][jj] = 0;
            R[0][jj] = 2;
        }
        for (ii = 1; ii <= n; ++ii) {
            for (jj = 1; jj <= m; ++jj) {
                if (this.oldL[ii - 1].equals(this.newL[jj - 1])) {
                    S[ii][jj] = S[ii - 1][jj - 1] + 1;
                    R[ii][jj] = 3;
                } else {
                    int distance = this.measure.getDistance(this.oldL[ii - 1], this.newL[jj - 1]);
                    if (distance > 0 && distance < 1000) {
                        S[ii][jj] = S[ii - 1][jj - 1] + 1;
                        R[ii][jj] = 4;
                    } else {
                        S[ii][jj] = S[ii - 1][jj - 1] + 0;
                        int n2 = R[ii][jj] = distance == 0 ? 3 : 0;
                    }
                }
                if (S[ii - 1][jj] >= S[ii][jj]) {
                    S[ii][jj] = S[ii - 1][jj];
                    R[ii][jj] = 1;
                }
                if (S[ii][jj - 1] < S[ii][jj]) {
                    continue;
                }
                S[ii][jj] = S[ii][jj - 1];
                R[ii][jj] = 2;
            }
        }
        ii = n;
        jj = m;
        if (!this.result.empty()) {
            this.result.clear();
        }
        while (ii > 0 || jj > 0) {
            E element;
            if (R[ii][jj] == 3) {
                --jj;
                element = this.oldL[--ii];
                this.result.push(new ResultItem<>(element, Operation.NOCHANGE));
                continue;
            }
            if (R[ii][jj] == 4) {
                --jj;
                element = this.newL[--ii];
                this.result.push(new ResultItem<>(element, Operation.MODIFY));
                continue;
            }
            if (R[ii][jj] == 1) {
                element = this.oldL[--ii];
                this.result.push(new ResultItem<>(element, Operation.DELETE));
                continue;
            }
            if (R[ii][jj] != 2) {
                continue;
            }
            element = this.newL[--jj];
            this.result.push(new ResultItem<>(element, Operation.INSERT));
        }
        return !this.result.empty();
    }

    ResultItem<E>[] getResult() {
        int size = this.result.size();
        ResultItem[] temp = new ResultItem[size];
        for (ResultItem<E> item : this.result) {
            temp[--size] = item;
        }
        return temp;
    }

    ResultItem<E>[] getTransformedResult() {
        Stack copy = (Stack)this.result.clone();
        ArrayList<ResultItem> temp = new ArrayList<>(copy.size());
        while (!copy.empty()) {
            ResultItem item = (ResultItem)copy.pop();
            if (item.operation == Operation.DELETE && !copy.empty() && ((ResultItem)copy.peek()).operation == Operation.INSERT) {
                ResultItem nextItem = (ResultItem)copy.pop();
                temp.add(new ResultItem(nextItem.element, Operation.MODIFY));
                continue;
            }
            temp.add(item);
        }
        return temp.toArray(new ResultItem[0]);
    }

    public String printResult(boolean transformed) {
        StringBuilder sb = new StringBuilder(128);
        ResultItem<E>[] temp = transformed ? this.getTransformedResult() : this.getResult();
        for (int i = 0; i < temp.length; ++i) {
            sb.append(temp[i]).append('\n');
        }
        return sb.toString();
    }

    static enum Operation {
        INSERT("insert"),
        MODIFY("modify"),
        DELETE("delete"),
        NOCHANGE("nochange");

        private final String name;

        private Operation(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    static final class ResultItem<S> {

        public final S element;
        public final Operation operation;

        public ResultItem(S element, Operation operation) {
            this.element = element;
            this.operation = operation;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append('{');
            sb.append((Object)this.operation);
            sb.append("} ");
            sb.append(this.element);
            return sb.toString();
        }

        int getChangeType() {
            switch (this.operation) {
                case DELETE: {
                    return 2;
                }
                case INSERT: {
                    return 1;
                }
                case MODIFY: {
                    return 0;
                }
                case NOCHANGE: {
                    return -1;
                }
            }
            throw new AssertionError();
        }
    }

}
