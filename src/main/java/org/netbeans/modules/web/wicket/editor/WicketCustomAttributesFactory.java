/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.web.wicket.editor;

import java.lang.reflect.Array;
import org.netbeans.modules.html.editor.api.gsf.CustomAttribute;

/**
 *
 * @author peter
 */
class WicketCustomAttributesFactory {

    static CustomAttribute[] create(String... attributes) {
        CustomAttribute[] res = (CustomAttribute[])Array.newInstance(CustomAttribute.class, attributes.length);
        for (int i = 0; i < attributes.length; i++) {
            res[i] = new WicketCustomAttribute(attributes[i]);
        }
        return res;
    }
}
