/*
 * Decompiled with CFR 0_130.
 */
package org.netbeans.modules.web.wicket.tree.borrowed;

public interface Change {
    public static final int INSERT = 1;
    public static final int DELETE = 2;
    public static final int CHANGE = 0;

    public int getType();

    public int getStart();

    public int getEnd();
}

