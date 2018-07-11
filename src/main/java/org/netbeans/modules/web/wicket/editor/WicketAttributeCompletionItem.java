/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.web.wicket.editor;

import org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem;
import org.netbeans.modules.html.editor.lib.api.HelpItem;

/**
 *
 * @author peter
 */
public class WicketAttributeCompletionItem extends HtmlCompletionItem.Attribute {

    public WicketAttributeCompletionItem(String value, int offset, boolean required, HelpItem helpItem) {
        super(value, offset, required, helpItem);
    }
}
