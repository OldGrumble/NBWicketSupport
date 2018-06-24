/*
 * Decompiled with CFR 0_130.
 */
package org.netbeans.modules.web.wicket.tree.borrowed;

public class Measure {
    static final Measure DEFAULT = new Measure();
    static final int INFINITE_DISTANCE = 1000;
    static final int OBJECTS_MATCH = 0;
    public static final Measure STRING = new StringMeasure();

    int getDistance(Object first, Object second) {
        assert (first != null && second != null);
        if (first == second || first.equals(second)) {
            return 0;
        }
        if (first instanceof Comparable && second instanceof Comparable) {
            Comparable c1 = (Comparable)first;
            Comparable c2 = (Comparable)second;
            return c1.compareTo(c2);
        }
        return 1000;
    }

    private static final class OrderedArrayMeasure
    extends Measure {
        private final Measure measure;

        OrderedArrayMeasure(Measure elementsMeasure) {
            this.measure = elementsMeasure;
        }

        @Override
        public int getDistance(Object first, Object second) {
            Object[] array1 = (Object[])first;
            Object[] array2 = (Object[])second;
            int minSize = Math.min(array1.length, array2.length);
            int difference = Math.abs(array1.length - array2.length);
            int result = 0;
            if (minSize == 0) {
                if (difference != 0) {
                    result = 1000;
                }
                return result;
            }
            for (int i = 0; i < minSize; ++i) {
                result += this.measure.getDistance(array1[i], array2[i]);
            }
            result += difference * 1000;
            return (result /= minSize + difference) > 1000 ? 1000 : result;
        }
    }

    private static final class StringMeasure
    extends Measure {
        private static final int SAME = 0;
        private static final int CASE_SAME = 1;
        private static final int DIFFERENT = 10;

        private StringMeasure() {
        }

        @Override
        public final int getDistance(Object first, Object second) {
            if (first == second) {
                return 0;
            }
            if (first == null || second == null) {
                return 1000;
            }
            String x = (String)first;
            String y = (String)second;
            int xlen = x.length();
            int ylen = y.length();
            int errors = 0;
            int xindex = 0;
            int yindex = 0;
            char[] xarr = new char[xlen + 1];
            char[] yarr = new char[ylen + 1];
            x.getChars(0, xlen, xarr, 0);
            y.getChars(0, ylen, yarr, 0);
            while (xindex < xlen && yindex < ylen) {
                char xchar = xarr[xindex];
                char ychar = yarr[yindex];
                int cherr = StringMeasure.compareChars(xchar, ychar);
                if (cherr != 10) {
                    errors += cherr;
                    ++xindex;
                    ++yindex;
                    continue;
                }
                char xchar1 = xarr[xindex + 1];
                char ychar1 = yarr[yindex + 1];
                if (xchar1 != '\u0000' && ychar1 != '\u0000') {
                    int cherr1 = StringMeasure.compareChars(xchar1, ychar1);
                    if (cherr1 != 10) {
                        errors += 10 + cherr1;
                        xindex += 2;
                        yindex += 2;
                        continue;
                    }
                    int xerr = StringMeasure.compareChars(xchar, ychar1);
                    int xerr1 = StringMeasure.compareChars(xchar1, ychar);
                    if (xerr != 10 && xerr1 != 10) {
                        errors += 10 + xerr + xerr1;
                        xindex += 2;
                        yindex += 2;
                        continue;
                    }
                }
                if (xlen - xindex > ylen - yindex) {
                    ++xindex;
                } else if (xlen - xindex < ylen - yindex) {
                    ++yindex;
                } else {
                    ++xindex;
                    ++yindex;
                }
                errors += 10;
            }
            return 1000 * (errors += (xlen - xindex + ylen - yindex) * 10) / Math.max(ylen, xlen) / 10;
        }

        private static final int compareChars(char xc, char yc) {
            char ylower;
            if (xc == yc) {
                return 0;
            }
            char xlower = Character.toLowerCase(xc);
            return xlower == (ylower = Character.toLowerCase(yc)) ? 1 : 10;
        }
    }

}

