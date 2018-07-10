/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.web.wicket.editor.model;

import org.netbeans.modules.csl.api.OffsetRange;

/**
 *
 * @author peter
 */
public class WicketToken {

    private final WicketNodeType ntype;
    private final OffsetRange range;

    WicketToken(WicketNodeType ntype, OffsetRange range) {
        this.ntype = ntype;
        this.range = range;
    }

    WicketToken(WicketNodeType ntype, int offset, int length) {
        this(ntype, new OffsetRange(offset, offset + length));
    }

    public WicketNodeType getNtype() {
        return ntype;
    }

    public OffsetRange getRange() {
        return range;
    }
}
