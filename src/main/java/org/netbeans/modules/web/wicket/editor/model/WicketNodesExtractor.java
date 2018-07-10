/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.web.wicket.editor.model;

import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;

/**
 *
 * @author peter
 */
public final class WicketNodesExtractor {

    public static WicketNodesModel getWicketNodesModel(HtmlParserResult result) {
        return new WicketNodesModel(result);
    }

    private WicketNodesExtractor() {
    }
}
