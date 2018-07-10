/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.web.wicket.editor.model;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;

/**
 *
 * @author peter
 */
public class WicketNodesModel {

    private final Map<WicketNodeType, List<WicketToken>> wicketTokens = new EnumMap<>(WicketNodeType.class);

    WicketNodesModel(HtmlParserResult result) {
    }

    public Map<WicketNodeType, List<WicketToken>> getWicketTokens() {
        return wicketTokens;
    }
}
