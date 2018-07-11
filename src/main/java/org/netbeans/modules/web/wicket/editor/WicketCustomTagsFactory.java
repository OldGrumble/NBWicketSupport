/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.web.wicket.editor;

import static org.netbeans.modules.web.wicket.util.WicketVersions.*;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.modules.html.editor.api.gsf.CustomTag;

/**
 *
 * @author peter
 */
class WicketCustomTagsFactory  {

    static Collection<CustomTag> createAll() {
        Set<CustomTag> res = new TreeSet<>();
        res.add(new WicketCustomTag("body"));
        res.add(new WicketCustomTag("border"));
        res.add(new WicketCustomTag("child"));
        res.add(new WicketCustomTag("container"));
        res.add(new WicketCustomTag("enclosure"));
        res.add(new WicketCustomTag("extend"));
        res.add(new WicketCustomTag("fragment"));
        res.add(new WicketCustomTag("head"));
        res.add(new WicketCustomTag("header-items", V6));
        res.add(new WicketCustomTag("label", V7));
        res.add(new WicketCustomTag("link"));
        res.add(new WicketCustomTag("message", "!key"));
        res.add(new WicketCustomTag("panel"));
        res.add(new WicketCustomTag("remove"));
        return res;
    }

}
