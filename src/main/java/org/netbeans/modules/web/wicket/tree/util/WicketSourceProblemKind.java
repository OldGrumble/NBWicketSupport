/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.tree.util;

import java.text.MessageFormat;
import org.openide.util.NbBundle;

/**
 *
 * @author Tim Boudreau
 */
public enum WicketSourceProblemKind {

    HTML_NODE_MISSING("H"),
    JAVA_NODE_MISSING("J"),
    DUPLICATE_HTML_IDS("H"),
    DUPLICATE_JAVA_IDS("J"),
    HTML_NODE_ADDED("H"),
    JAVA_NODE_ADDED("J"),
    DIFFERENT_IDS("JH");

    private final boolean htmlProblem;
    private final boolean javaProblem;
    private String messageTemplate;

    private WicketSourceProblemKind(String problemType) {
        htmlProblem = problemType.contains("H");
        javaProblem = problemType.contains("J");
    }

    public boolean isHtmlProblem() {
        return htmlProblem;
    }

    public boolean isJavaProblem() {
        return javaProblem;
    }

    public String getMessageTemplate() {
        if (messageTemplate == null) {
            messageTemplate = NbBundle.getMessage(WicketSourceProblemKind.class, name());
        }
        return messageTemplate;
    }

    public String getMessage(Object... params) {
        return MessageFormat.format(getMessageTemplate(), params);
    }
}
