/*
 * Some license issues have still to be clarified, especially for the "borrowed"
 * package, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.tree.borrowed;

/*
TODO-PROTECT: Verwendet in tree.TreeDiff
*/
public interface Change {

    public static final int INSERT = 1;
    public static final int DELETE = 2;
    public static final int CHANGE = 0;

    public int getType();

    public int getStart();

    public int getEnd();
}
