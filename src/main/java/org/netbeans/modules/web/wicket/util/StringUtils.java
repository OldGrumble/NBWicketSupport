/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.util;

/**
 *
 * @author Tim Boudreau
 * @author Peter Nabbefeld
 */
public class StringUtils {

    public static String unquote(String quoted) {
        if (quoted == null) {
            return "?";
        }
        char c = quoted.charAt(0);
        if (c == '\"' || c == '\'') {
            quoted = quoted.substring(1);
        }
        if ((c = quoted.charAt(quoted.length() - 1)) == '\"' || c == '\'') {
            quoted = quoted.substring(0, quoted.length() - 1);
        }
        return quoted;
    }
}
