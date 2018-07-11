/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.web.wicket.editor;

import java.util.Arrays;
import org.netbeans.modules.web.wicket.util.WicketVersions;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import org.netbeans.modules.html.editor.api.gsf.CustomAttribute;
import org.netbeans.modules.html.editor.api.gsf.CustomTag;
import org.netbeans.modules.html.editor.lib.api.HelpItem;

/**
 *
 * @author peter
 */
class WicketCustomTag implements CustomTag, Comparable<WicketCustomTag> {

    private final String name;
    private final WicketVersions version;
    private final CustomAttribute[] attributes;

    public WicketCustomTag(String name, WicketVersions version, String... attributes) {
        assert name != null;
        this.name = (name.indexOf(':') >= 0) ? name : "wicket:" + name;
        this.version = version == null ? WicketVersions.ALL : version;
        if (attributes.length == 0) {
            this.attributes = new CustomAttribute[0];
        } else {
            this.attributes = WicketCustomAttributesFactory.create(attributes);
        }
    }

    public WicketCustomTag(String name, String... attributes) {
        this(name, WicketVersions.ALL, attributes);
    }

    public WicketVersions getVersion() {
        return version;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public HelpItem getHelp() {
        return null;
    }

    @Override
    public Collection<CustomAttribute> getAttributes() {
        return Collections.unmodifiableCollection(Arrays.asList(attributes));
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WicketCustomTag other = (WicketCustomTag)obj;
        return this.compareTo(other) == 0;
    }

    @Override
    public int compareTo(WicketCustomTag other) {
        if (name.equals(other.name)) {
            ensurePayloadEquals(other);
        }
        return name.compareTo(other.name);
    }

    private void ensurePayloadEquals(WicketCustomTag o) {
        if (version != o.version || !Arrays.equals(attributes, o.attributes)) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
}
