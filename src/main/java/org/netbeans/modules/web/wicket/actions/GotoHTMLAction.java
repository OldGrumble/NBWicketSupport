/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.wicket.MarkupForJavaQuery;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.Utilities;

/**
 *
 * @author Tim Boudreau (original author)
 * @author Peter Nabbefeld
 */
@ActionID(
        id = "org.netbeans.modules.web.wicket.actions.GotoHTMLAction",
        category = "Wicket"
)
@ActionRegistration(displayName = "#CTL_GotoHTMLAction", lazy = false)
public class GotoHTMLAction extends AbstractGotoAction {

    private static final long serialVersionUID = 1L;

    private Lookup context;
    Lookup.Result<DataObject> lkpInfo;

    public GotoHTMLAction() {
        this(Utilities.actionsGlobalContext());
    }

    private GotoHTMLAction(Lookup context) {
//        putValue(Action.NAME, NbBundle.getMessage(GotoHTMLAction.class, "CTL_GotoHTMLAction"));
        this.context = context;
    }

    void init() {
        assert SwingUtilities.isEventDispatchThread() : "this shall be called just from AWT thread";

        if (lkpInfo != null) {
            return;
        }

        // The thing we want to listen for the presence or absence of
        // on the global selection
        lkpInfo = context.lookupResult(DataObject.class);
        lkpInfo.addLookupListener(this);
        resultChanged(null);
    }

    /**
     * Open the HTML file.
     *
     * @param evt The ActionEvent triggering this method.
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        init();
        for (DataObject dObj : lkpInfo.allInstances()) {
            FileObject fo = MarkupForJavaQuery.find(dObj.getPrimaryFile());
            try {
                DataObject htmlDobj = DataObject.find(fo);
                OpenCookie oc = (OpenCookie)htmlDobj.getLookup().lookup(OpenCookie.class);
                oc.open();
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /**
     * Enable this action only if Wicket framework is added to this project and
     * the selected Java file has a HTML counterpart.
     *
     * @return True, if this action should be enabled.
     */
    @Override
    public boolean isEnabled() {
        init();
        return lkpInfo != null && lkpInfo.allInstances().size() == 1 && inWicketProject(lkpInfo.allInstances().iterator().next());
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        setEnabled(!lkpInfo.allInstances().isEmpty());
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new GotoHTMLAction(context);
    }
}
