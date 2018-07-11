/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.web.wicket.editor;

import org.netbeans.modules.html.editor.api.gsf.CustomAttribute;
import org.netbeans.modules.html.editor.lib.api.HelpItem;

/**
 *
 * @author peter
 */
class WicketCustomAttribute implements CustomAttribute {

    private final String name;
    private final boolean required;

    public WicketCustomAttribute(String name) {
        this.required = name.startsWith("!");
        this.name = getNameInternal(name, required ? 1 : 0);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Override
    public boolean isValueRequired() {
        return true;
    }

    @Override
    public HelpItem getHelp() {
        return null;
    }

    private String getNameInternal(String name, int offset) {
        String localName = name.substring(offset);
        return (localName.indexOf(':') >= 0) ? name : "wicket:" + name;
    }
}
