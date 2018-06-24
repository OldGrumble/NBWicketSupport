/*
 * Decompiled with CFR 0_130.
 * 
 * Could not load the following classes:
 *  org.openide.text.Annotation
 */
package org.netbeans.modules.web.wicket.verification;

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
