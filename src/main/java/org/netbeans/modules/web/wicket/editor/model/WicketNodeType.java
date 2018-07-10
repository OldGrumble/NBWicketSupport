/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.web.wicket.editor.model;

import java.util.Set;
import org.netbeans.modules.csl.api.ColoringAttributes;

/**
 *
 * @author peter
 */
public enum WicketNodeType {

    ID_ATTRIBUTE(ColoringAttributes.PARAMETER_SET);

    private final Set<ColoringAttributes> coloringAttributes;

    private WicketNodeType(Set<ColoringAttributes> coloringAttributes) {
        this.coloringAttributes = coloringAttributes;
    }

    public Set<ColoringAttributes> getColoringAttributes() {
        return coloringAttributes;
    }
}
