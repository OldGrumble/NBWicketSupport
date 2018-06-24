// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) lnc 
// Source File Name:   GotoHTMLAction.java
package org.netbeans.modules.web.wicket.actions;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.wicket.MarkupForJavaQuery;
import org.netbeans.api.wicket.WicketProjectQuery;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.actions.CookieAction;

@ActionID(
        id = "org.netbeans.modules.web.wicket.actions.GotoHTMLAction",
        category = "Wicket"
)
@ActionRegistration(displayName = "#CTL_GotoHTMLAction", lazy = false)
public class GotoHTMLAction extends CookieAction {

    public GotoHTMLAction() {
    }

    /**
     * Open the HTML file.
     *
     * @param activatedNodes Activated Java file(s).
     */
    @Override
    protected void performAction(Node activatedNodes[]) {
        DataObject dObj = (DataObject)activatedNodes[0].getLookup().lookup(DataObject.class);
        FileObject fo = MarkupForJavaQuery.find(dObj.getPrimaryFile());
        try {
            DataObject htmlDobj = DataObject.find(fo);
            OpenCookie oc = (OpenCookie)htmlDobj.getLookup().lookup(OpenCookie.class);
            oc.open();
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Enable this action only if Wicket framework is added to this project and
     * the selected Java file has a HTML counterpart.
     *
     * @param activatedNodes
     * @return
     */
    @Override
    protected boolean enable(Node activatedNodes[]) {
        DataObject dObj = (DataObject)activatedNodes[0].getLookup().lookup(DataObject.class);
        FileObject fo = MarkupForJavaQuery.find(dObj.getPrimaryFile());
        if (fo != null) {
            Project proj = FileOwnerQuery.getOwner(fo);
            return proj != null && WicketProjectQuery.isWicket(proj);
        } else {
            return false;
        }
    }

    /**
     * This action may only be used if exactly one node is selected.
     * 
     * @return The selection mode.
     */
    @Override
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(GotoHTMLAction.class, "CTL_GotoHTMLAction");
    }

    @Override
    protected Class[] cookieClasses() {
        return (new Class[]{
            EditorCookie.class
        });
    }

    @Override
    protected void initialize() {
        super.initialize();
        putValue("noIconInMenu", Boolean.TRUE);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
