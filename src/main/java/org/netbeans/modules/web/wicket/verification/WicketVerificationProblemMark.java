/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.verification;

/**
 * 
 * @author Tim Boudreau
 */
import org.openide.text.Annotation;

public class WicketVerificationProblemMark extends Annotation {

    private final String errorMessage;

    public WicketVerificationProblemMark(String message) {
        this.errorMessage = message;
    }

    @Override
    public String getAnnotationType() {
        return "org-netbeans-modules-web-wicket-verification-WicketProblemMark";
    }

    @Override
    public String getShortDescription() {
        return this.errorMessage;
    }
}
