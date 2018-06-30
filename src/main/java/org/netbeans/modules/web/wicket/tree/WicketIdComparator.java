/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.web.wicket.tree;

import java.util.Comparator;

/**
 *
 * @author peter
 */
public final class WicketIdComparator implements Comparator<Node> {

    @Override
    public int compare(Node a, Node b) {
        return a.getId().compareTo(b.getId());
    }
}
