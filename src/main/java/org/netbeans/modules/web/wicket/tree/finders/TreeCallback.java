/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.tree.finders;

import org.netbeans.modules.web.wicket.tree.MarkupContainerTree;

/**
 *
 * @author Tim Boudreau
 */
public interface TreeCallback {

    public void setTree(MarkupContainerTree<String> mct);
}
