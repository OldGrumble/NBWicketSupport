package org.netbeans.modules.web.wicket.actions;

import javax.swing.AbstractAction;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.wicket.MarkupForJavaQuery;
import org.netbeans.api.wicket.WicketProjectQuery;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.LookupListener;

/**
 *
 * @author Peter Nabbefeld
 */
public abstract class AbstractGotoAction extends AbstractAction implements LookupListener, ContextAwareAction {

    private static final long serialVersionUID = 1L;

    protected boolean inWicketProject(DataObject dObj) {
        FileObject fo = MarkupForJavaQuery.find(dObj.getPrimaryFile());
        if (fo != null) {
            Project proj = FileOwnerQuery.getOwner(fo);
            return proj != null && WicketProjectQuery.isWicket(proj);
        } else {
            return false;
        }
    }

}
