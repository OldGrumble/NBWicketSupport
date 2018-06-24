// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) lnc
// Source File Name:   Installer.java
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
 * @author Gj
 */
public class Installer extends ModuleInstall {

    /**
     * TODO: Probably this installs/opens some demo project?
     */
    private class DemoRunnable implements Runnable {

        private DemoRunnable() {
        }

        /**
         * TODO: Could not find out, when the Runnable is started and what's its
         * use.
         */
        @Override
        public void run() {
            WindowManager.getDefault().getRegistry().addPropertyChangeListener((PropertyChangeEvent evt) -> {
                if (evt.getPropertyName().equals("opened")) {
                    HashSet newHashSet = (HashSet)evt.getNewValue();
                    HashSet oldHashSet = (HashSet)evt.getOldValue();
                    Iterator it = newHashSet.iterator();
                    while (it.hasNext()) {
                        TopComponent topComponent = (TopComponent)it.next();
                        if (!oldHashSet.contains(topComponent)) {
                            DataObject dObj = (DataObject)topComponent.getLookup().lookup(DataObject.class);
                            if (dObj != null) {
                                FileObject currentFile = dObj.getPrimaryFile();
                                if (currentFile != null) {
                                    String mimeType = dObj.getPrimaryFile().getMIMEType();
                                    FileObject matchingFile = null;
                                    if (mimeType.equals(WicketSupportConstants.MIME_TYPE_HTML)) {
                                        matchingFile = JavaForMarkupQuery.find(currentFile);
                                    } else if (mimeType.equals(WicketSupportConstants.MIME_TYPE_JAVA)) {
                                        matchingFile = MarkupForJavaQuery.find(currentFile);
                                    }
                                    if (matchingFile != null) {
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
        WindowManager.getDefault().invokeWhenUIReady(new DemoRunnable());
    }
}
