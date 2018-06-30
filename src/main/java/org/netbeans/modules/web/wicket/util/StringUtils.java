/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.web.wicket.util;

/**
 *
 * @author peter
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
