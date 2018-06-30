/*
 * Some license issues have still to be clarified, especially for the "borrowed"
 * package, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.tree.finders;

import org.netbeans.modules.web.wicket.tree.MarkupContainerTree;

/**
 *
 * @author peter
 */
public interface TreeCallback {

    public void setTree(MarkupContainerTree<String> mct);
}
