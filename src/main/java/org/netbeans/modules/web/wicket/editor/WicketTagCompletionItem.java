/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.web.wicket.editor;

import org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem;

/**
 *
 * @author peter
 */
public class WicketTagCompletionItem extends HtmlCompletionItem.Tag {

    public WicketTagCompletionItem(String text, int substitutionOffset) {
        super(text, substitutionOffset, null, true);
    }
}
