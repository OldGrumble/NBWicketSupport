/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.actions;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.wicket.JavaForMarkupQuery;
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

/**
 *
 * @author Tim Boudreau
 */
@ActionID(
        id = "org.netbeans.modules.web.wicket.actions.GotoJavaAction",
        category = "Wicket"
)
@ActionRegistration(displayName = "#CTL_GotoJavaAction", lazy = false)
public class GotoJavaAction extends CookieAction {

    public GotoJavaAction() {
    }

    @Override
    protected void performAction(Node activatedNodes[]) {
        DataObject dObj = (DataObject)activatedNodes[0].getLookup().lookup(DataObject.class);
        FileObject fo = JavaForMarkupQuery.find(dObj.getPrimaryFile());
        try {
            DataObject javaDobj = DataObject.find(fo);
            OpenCookie oc = (OpenCookie)javaDobj.getCookie(OpenCookie.class);
            oc.open();
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    protected boolean enable(Node activatedNodes[]) {
        DataObject dObj = (DataObject)activatedNodes[0].getLookup().lookup(DataObject.class);
        FileObject fo = JavaForMarkupQuery.find(dObj.getPrimaryFile());
        if (fo != null) {
            Project proj = FileOwnerQuery.getOwner(fo);
            return proj != null && WicketProjectQuery.isWicket(proj);
        } else {
            return false;
        }
    }

    @Override
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(GotoJavaAction.class, "CTL_GotoJavaAction");
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
