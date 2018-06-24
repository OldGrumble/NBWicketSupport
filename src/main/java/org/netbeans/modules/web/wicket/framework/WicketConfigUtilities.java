/*
 * Decompiled with CFR 0_130.
 * 
 * Could not load the following classes:
 *  org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
 *  org.netbeans.modules.j2ee.dd.api.web.DDProvider
 *  org.netbeans.modules.j2ee.dd.api.web.Filter
 *  org.netbeans.modules.j2ee.dd.api.web.FilterMapping
 *  org.netbeans.modules.j2ee.dd.api.web.WebApp
 *  org.openide.filesystems.FileObject
 */
package org.netbeans.modules.web.wicket.framework;

import java.io.File;
import java.io.IOException;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Filter;
import org.netbeans.modules.j2ee.dd.api.web.FilterMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.openide.filesystems.FileObject;

public class WicketConfigUtilities {

    private static final String WICKET_JAR = "wicket";
    private static final String LIBS_FOLDER = "org-netbeans-api-project-libraries/Libraries";
    private static final String LIB_WICKET_FILE = "wicket.xml";
    public static String DEFAULT_MODULE_NAME = "config";
    private static final int TYPE_ACTION = 0;
    private static final int TYPE_FORM_BEAN = 1;
    private static final int TYPE_MESSAGE_RESOURCES = 2;

    public static Filter getWicketFilter(FileObject dd) {
        if (dd == null) {
            return null;
        }
        try {
            WebApp webApp = DDProvider.getDefault().getDDRoot(dd);
            return (Filter)webApp.findBeanByName("Filter", "FilterClass", "org.apache.wicket.protocol.http.WicketFilter");
        } catch (IOException e) {
            return null;
        }
    }

    public static String getWicketFilterMapping(FileObject dd) {
        Filter filter = WicketConfigUtilities.getWicketFilter(dd);
        if (filter != null) {
            try {
                WebApp webApp = DDProvider.getDefault().getDDRoot(dd);
                FilterMapping[] mappings = webApp.getFilterMapping();
                for (int i = 0; i < mappings.length; ++i) {
                    if (!mappings[i].getFilterName().equals(filter.getFilterName())) {
                        continue;
                    }
                    return mappings[i].getUrlPattern();
                }
            } catch (IOException e) {
                // empty catch block
            }
        }
        return null;
    }

    public static boolean isWicketInstallFolder(File folder) {
        boolean result = false;
        String fileSeparator = System.getProperty("file.separator");
        if (folder.exists() && folder.isDirectory()) {
            File[] child = folder.listFiles();
            for (int i = 0; i < child.length && !result; ++i) {
                if (!child[i].getName().startsWith(WICKET_JAR) || !child[i].getName().endsWith(".jar")) {
                    continue;
                }
                result = true;
            }
        }
        return result;
    }
}
