/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.installer;

import java.beans.PropertyChangeEvent;
import java.util.HashSet;
import java.util.Iterator;
import org.netbeans.api.wicket.JavaForMarkupQuery;
import org.netbeans.api.wicket.MarkupForJavaQuery;
import org.netbeans.modules.web.wicket.util.WicketSupportConstants;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Installer class.
 *
 * @author Tim Boudreau
 */
public class Installer extends ModuleInstall {

    private static final long serialVersionUID = 1L;

    /**
     * This class installs a PropertyChangeListener for opening the counterpart
     * file when the run method is executed.
     */
    private class MatchingFileOpenerRunnable implements Runnable {

        private MatchingFileOpenerRunnable() {
        }

        /**
         * This method installs a PropertyChangeListener for opening the
         * counterpart file.
         */
        @Override
        public void run() {
            WindowManager.getDefault().getRegistry().addPropertyChangeListener((PropertyChangeEvent evt) -> {
                if (evt.getPropertyName().equals("opened")) {
                    // Execute only, if the PropertyChange was fired because of opening a TopComponent.
                    HashSet<TopComponent> newHashSet = (HashSet<TopComponent>)evt.getNewValue();
                    HashSet<TopComponent> oldHashSet = (HashSet<TopComponent>)evt.getOldValue();
                    Iterator<TopComponent> it = newHashSet.iterator();
                    while (it.hasNext()) {
                        TopComponent topComponent = it.next();
                        if (!oldHashSet.contains(topComponent)) {
                            // This TopComponent was not open before.
                            DataObject dObj = (DataObject)topComponent.getLookup().lookup(DataObject.class);
                            if (dObj != null) {
                                FileObject currentFile = dObj.getPrimaryFile();
                                if (currentFile != null) {
                                    String mimeType = dObj.getPrimaryFile().getMIMEType();
                                    FileObject matchingFile = null;
                                    if (mimeType.equals(WicketSupportConstants.MIME_TYPE_HTML)) {
                                        // A HTML file was opened; get Java counterpart if Wicket framework is used.
                                        matchingFile = JavaForMarkupQuery.find(currentFile);
                                    } else if (mimeType.equals(WicketSupportConstants.MIME_TYPE_JAVA)) {
                                        // A Java file was opened; get HTML counterpart if Wicket framework is used
                                        matchingFile = MarkupForJavaQuery.find(currentFile);
                                    }
                                    if (matchingFile != null) {
                                        // The TopWindow contains an editor for a file using Wicket framework,
                                        // the counterpart is opened, too.
                                        try {
                                            DataObject matchingDobj = DataObject.find(matchingFile);
                                            OpenCookie oc = (OpenCookie)matchingDobj.getLookup().lookup(OpenCookie.class);
                                            oc.open();
                                        } catch (DataObjectNotFoundException ex) {
                                            Exceptions.printStackTrace(ex);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    public Installer() {
    }

    @Override
    public void restored() {
        WindowManager.getDefault().invokeWhenUIReady(new MatchingFileOpenerRunnable());
    }
}
