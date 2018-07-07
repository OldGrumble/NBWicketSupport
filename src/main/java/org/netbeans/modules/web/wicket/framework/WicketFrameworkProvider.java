/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.framework;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.SourceGroupModifier;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Filter;
import org.netbeans.modules.j2ee.dd.api.web.FilterMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WelcomeFileList;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.ErrorManager;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.MapFormat;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Tim Boudreau
 */
public class WicketFrameworkProvider extends WebFrameworkProvider {

    private static final String defaultAppResource = "Application";
    private static final String defaultPkgResource = "com.myapp.wicket";
    private static String defaultWebPageResource = "HomePage";
    private WicketWebModuleExtender panel;

    public WicketFrameworkProvider() {
        super(NbBundle.getMessage(WicketFrameworkProvider.class, "Wicket_Name"), NbBundle.getMessage(WicketFrameworkProvider.class, "Wicket_Description"));
    }

    public Set extendImpl(WebModule webModule) {
        HashSet result = new HashSet();
        try {
            FileObject webInf;
            FileObject[] javaSources = getJavaSources(webModule);
            String libName = panel.getWicketVersion() != null ? panel.getWicketVersion() : panel.getComponent().getWicketVersion();
            Library lib = LibraryManager.getDefault().getLibrary(libName);
            if (lib != null) {
                ProjectClassPathModifier.addLibraries((Library[])new Library[]{lib}, javaSources[0], "classpath/compile");
            }
            if ((webInf = webModule.getWebInf()) == null) {
                webInf = FileUtil.createFolder((FileObject)webModule.getDocumentBase(), "WEB-INF");
            }
            assert (webInf != null);
            FileSystem fileSystem = webInf.getFileSystem();
            fileSystem.runAtomicAction((FileSystem.AtomicAction)new CreateWicketFiles(webModule));
        } catch (IOException ex) {
            Exceptions.printStackTrace((Throwable)ex);
        }
        return result;
    }

    void addLibrary(String pathToWicketFolder) throws FileNotFoundException, IOException {
        final String coreLibPrefix = "wicket-core-";
        final String jarFileSuffix = ".jar";
        final File wicketDir = new File(pathToWicketFolder);
        if (!wicketDir.exists()) {
            throw new FileNotFoundException();
        }
        if (!wicketDir.isDirectory()) {
            throw new IOException();
        }
        File[] files = wicketDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                try {
                    return dir.getCanonicalFile().equals(wicketDir.getCanonicalFile()) && name.startsWith(coreLibPrefix) && name.endsWith(jarFileSuffix);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                    return false;
                }
            }
        });
        if (files.length != 1) {
            throw new IOException();
        }
        LibraryTypeProvider[] providers = LibrariesSupport.getLibraryTypeProviders();
        for (LibraryTypeProvider provider : providers) {
            System.out.println("ProviderType: " + provider + ", '" + provider.getLibraryType() + "', " + provider.getDisplayName());
            String[] supportedVolumeTypes = provider.getSupportedVolumeTypes();
            System.out.println("supportedVolumeTypes = " + Arrays.asList(supportedVolumeTypes));
        }
        System.out.println();
//        J2SELibraryTypeProvider jp;
        String libName = files[0].getName();
        String version = libName.substring(coreLibPrefix.length(), libName.length() - jarFileSuffix.length());
        Map<String, List<URL>> contents = new TreeMap<>();
        String volume = "classpath";
        files = wicketDir.listFiles((File dir, String name1) -> {
            try {
                return dir.getCanonicalFile().equals(wicketDir.getCanonicalFile()) && name1.endsWith(jarFileSuffix);
            }catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return false;
            }
        });
        List<URL> list;
        for (File file : files) {
            list = contents.get(volume);
            if (list == null) {
                list = new ArrayList<>();
                contents.put(volume, list);
            }
            list.add(Utilities.toURI(file).toURL());
        }
        LibraryManager.getDefault().createLibrary("j2se", "Wicket " + version, contents);
    }

    @Override
    public boolean isInWebModule(WebModule webModule) {
        FileObject dd = webModule.getDeploymentDescriptor();
        return dd != null && WicketConfigUtilities.getWicketFilter(dd) != null;
    }

    public static String readResource(InputStream is, String encoding) throws IOException {
        StringBuilder sb = new StringBuilder();
        String lineSep = System.getProperty("line.separator");
        BufferedReader br = new BufferedReader(new InputStreamReader(is, encoding));
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            sb.append(lineSep);
            line = br.readLine();
        }
        br.close();
        return sb.toString();
    }

    @Override
    public File[] getConfigurationFiles(WebModule webModule) {
        return null;
    }

    @Override
    public WebModuleExtender createWebModuleExtender(WebModule wm, ExtenderController controller) {
        boolean useDefaultValue = wm == null || !this.isInWebModule(wm);
        this.panel = new WicketWebModuleExtender(this, controller, useDefaultValue);
        if (useDefaultValue) {
            this.panel.setAppResource(defaultAppResource);
            this.panel.setPkgResource(defaultPkgResource);
            this.panel.setWebPageResource(defaultWebPageResource);
        } else {
            String applicationClassValue;
            FileObject dd = wm.getDeploymentDescriptor();
            Filter filter = WicketConfigUtilities.getWicketFilter(dd);
            String applicationClassName = "applicationClassName";
            InitParam initParam = (InitParam)filter.findBeanByName("InitParam", "ParamName", applicationClassName);
            this.panel.setServletName(filter.getFilterName());
            this.panel.setURLPattern(WicketConfigUtilities.getWicketFilterMapping(dd));
            if (initParam != null && (applicationClassValue = initParam.getParamValue()) != null) {
                String appResource = applicationClassValue.substring(applicationClassValue.lastIndexOf(".") + 1);
                String pkgResource = applicationClassValue.substring(0, applicationClassValue.lastIndexOf("."));
                this.panel.setAppResource(appResource);
                this.panel.setPkgResource(pkgResource);
                this.panel.setWebPageResource(defaultWebPageResource);
            }
        }
        return this.panel;
    }

    private static EditableProperties loadProperties(FileObject propsFO) throws IOException {
        EditableProperties props;
        InputStream propsIS = propsFO.getInputStream();
        props = new EditableProperties(true);
        try {
            props.load(propsIS);
        } finally {
            propsIS.close();
        }
        return props;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void storeProperties(FileObject propsFO, EditableProperties props) throws IOException {
        FileLock lock = propsFO.lock();
        try (OutputStream os = propsFO.getOutputStream(lock)) {
            props.store(os);
        } finally {
            lock.releaseLock();
        }
    }

    private class CreateWicketFiles implements FileSystem.AtomicAction {

        WebModule wm;

        public CreateWicketFiles(WebModule wm) {
            this.wm = wm;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private void createFile(FileObject target, String content, String encoding) throws IOException {
            FileLock lock = target.lock();
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(target.getOutputStream(lock), encoding))) {
                bw.write(content);
            } finally {
                lock.releaseLock();
            }
        }

        @Override
        public void run() throws IOException {
            try {
                FileObject targetResFolder;
                OpenCookie openJavaHomePage;
                DataObject javaHomePage;
                String content = WicketFrameworkProvider.readResource(FileUtil.getConfigFile((String)"org-netbeans-modules-web-wicket-template/WicketApplication.template").getInputStream(), "UTF-8");
                HashMap<String, String> args = new HashMap<>();
                args.put("USER", System.getProperty("user.name"));
                args.put("NAME", WicketFrameworkProvider.this.panel.getAppResource().replace('.', '/'));
                args.put("DATE", DateFormat.getDateInstance(1).format(new Date()));
                args.put("TIME", DateFormat.getTimeInstance(3).format(new Date()));
                args.put("NAME_OF_PACKAGE", WicketFrameworkProvider.this.panel.getPkgResource());
                args.put("NAME_OF_HOME_PAGE", WicketFrameworkProvider.this.panel.getWebPageResource().replace('.', '/'));
                defaultWebPageResource = WicketFrameworkProvider.this.panel.getWebPageResource().replace('.', '/');
                MapFormat formater = new MapFormat(args);
                formater.setLeftBrace("__");
                formater.setRightBrace("__");
                formater.setExactMatch(false);
                content = formater.format((Object)content);
                Project project = FileOwnerQuery.getOwner((FileObject)this.wm.getDocumentBase());
                SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups("java");
                String path = WicketFrameworkProvider.this.panel.getPkgResource();
                String name = WicketFrameworkProvider.this.panel.getAppResource();
                name = name + ".java";
                FileObject targetFolder = sourceGroups[0].getRootFolder();
                String[] folders = path.split("\\.");
                for (int i = 0; i < folders.length; ++i) {
                    targetFolder = targetFolder.getFileObject(folders[i]) == null ? targetFolder.createFolder(folders[i]) : targetFolder.getFileObject(folders[i]);
                }
                FileObject target = FileUtil.createData((FileObject)targetFolder, (String)name);
                this.createFile(target, content, "UTF-8");
                SourceGroup resGroup = SourceGroupModifier.createSourceGroup(project, (String)"resources", (String)"main");
                if (resGroup != null) {
                    targetResFolder = resGroup.getRootFolder();
                    String pth = path.replace('.', '/');
                    targetResFolder = FileUtil.createFolder((FileObject)targetResFolder, (String)pth);
                } else {
                    targetResFolder = targetFolder;
                }
                String defaultWicketApplicationPage_html = WicketFrameworkProvider.this.panel.getWebPageResource() + ".html";
                content = WicketFrameworkProvider.readResource(FileUtil.getConfigFile((String)"org-netbeans-modules-web-wicket-template/WicketApplicationPage.html").getInputStream(), "UTF-8");
                target = FileUtil.createData((FileObject)targetResFolder, (String)defaultWicketApplicationPage_html);
                this.createFile(target, content, "UTF-8");
                String defaultWicketExampleHeader_html = "HeaderPanel.html";
                content = WicketFrameworkProvider.readResource(FileUtil.getConfigFile((String)"org-netbeans-modules-web-wicket-template/WicketExampleHeader.html").getInputStream(), "UTF-8");
                FileObject fileToOpen = target = FileUtil.createData((FileObject)targetResFolder, (String)defaultWicketExampleHeader_html);
                this.createFile(target, content, "UTF-8");
                String defaultWicketApplicationPage_java = WicketFrameworkProvider.this.panel.getWebPageResource() + ".java";
                content = WicketFrameworkProvider.readResource(FileUtil.getConfigFile((String)"org-netbeans-modules-web-wicket-template/WicketApplicationPage.template").getInputStream(), "UTF-8");
                args.put("NAME", WicketFrameworkProvider.this.panel.getWebPageResource());
                args.put("NAME_OF_IMPORT_CLASS", "");
                args.put("NAME_OF_IMPORT_CLASS_SHORT", "BasePage");
                content = formater.format((Object)content);
                target = FileUtil.createData((FileObject)targetFolder, (String)defaultWicketApplicationPage_java);
                this.createFile(target, content, "UTF-8");
                String defaultWicketExampleHeader_java = "HeaderPanel.java";
                content = WicketFrameworkProvider.readResource(FileUtil.getConfigFile((String)"org-netbeans-modules-web-wicket-template/WicketExampleHeader.template").getInputStream(), "UTF-8");
                content = formater.format((Object)content);
                target = FileUtil.createData((FileObject)targetFolder, (String)defaultWicketExampleHeader_java);
                this.createFile(target, content, "UTF-8");
                String defaultFooterPage_html = "FooterPanel.html";
                content = WicketFrameworkProvider.readResource(FileUtil.getConfigFile((String)"org-netbeans-modules-web-wicket-template/WicketExampleFooter.html").getInputStream(), "UTF-8");
                target = FileUtil.createData((FileObject)targetResFolder, (String)defaultFooterPage_html);
                this.createFile(target, content, "UTF-8");
                String defaultBasePage_html = "BasePage.html";
                content = WicketFrameworkProvider.readResource(FileUtil.getConfigFile((String)"org-netbeans-modules-web-wicket-template/WicketExamplePage.html").getInputStream(), "UTF-8");
                target = FileUtil.createData((FileObject)targetResFolder, (String)defaultBasePage_html);
                this.createFile(target, content, "UTF-8");
                String defaultBasePage_java = "BasePage.java";
                content = WicketFrameworkProvider.readResource(FileUtil.getConfigFile((String)"org-netbeans-modules-web-wicket-template/WicketExamplePage.template").getInputStream(), "UTF-8");
                content = formater.format((Object)content);
                target = FileUtil.createData((FileObject)targetFolder, (String)defaultBasePage_java);
                this.createFile(target, content, "UTF-8");
                String defaultFooterPage_java = "FooterPanel.java";
                content = WicketFrameworkProvider.readResource(FileUtil.getConfigFile((String)"org-netbeans-modules-web-wicket-template/WicketExampleFooter.template").getInputStream(), "UTF-8");
                content = formater.format((Object)content);
                target = FileUtil.createData((FileObject)targetFolder, (String)defaultFooterPage_java);
                this.createFile(target, content, "UTF-8");
                String j2eeLevel = WicketFrameworkProvider.this.panel.getController().getProperties().getProperty("j2eeLevel").toString();
                FileObject projectprop = project.getProjectDirectory().getFileObject("nbproject/project.properties");
                if (j2eeLevel.equals("1.5") || j2eeLevel.equals("1.6-web")) {
                    FileObject dd = this.wm.getDeploymentDescriptor();
                    WebApp ddRoot = DDProvider.getDefault().getDDRootCopy(dd);
                    if (ddRoot != null) {
                        try {
                            Filter filter = (Filter)ddRoot.createBean("Filter");
                            filter.setFilterName("WicketApplication");
                            filter.setFilterClass("org.apache.wicket.protocol.http.WicketFilter");
                            ddRoot.addFilter(filter);
                            InitParam appClassNameParam = (InitParam)filter.createBean("InitParam");
                            appClassNameParam.setParamName("applicationClassName");
                            appClassNameParam.setParamValue(WicketFrameworkProvider.this.panel.getPkgResource() + "." + WicketFrameworkProvider.this.panel.getAppResource());
                            filter.addInitParam(appClassNameParam);
                            FilterMapping mapping = (FilterMapping)ddRoot.createBean("FilterMapping");
                            mapping.setFilterName(WicketFrameworkProvider.this.panel.getServletName());
                            mapping.setUrlPattern(WicketFrameworkProvider.this.panel.getURLPattern());
                            ddRoot.addFilterMapping(mapping);
                            WelcomeFileList welcomeFiles = (WelcomeFileList)ddRoot.createBean("WelcomeFileList");
                            welcomeFiles.setWelcomeFile(new String[]{""});
                            ddRoot.setWelcomeFileList(welcomeFiles);
                            ddRoot.write(dd);
                        } catch (ClassNotFoundException cnfe) {
                            ErrorManager.getDefault().notify((Throwable)cnfe);
                        }
                    }
                }
                String style_css = "style.css";
                content = WicketFrameworkProvider.readResource(FileUtil.getConfigFile((String)"org-netbeans-modules-web-wicket-template/style.css").getInputStream(), "UTF-8");
                target = FileUtil.createData((FileObject)targetResFolder, (String)style_css);
                this.createFile(target, content, "UTF-8");
                FileObject documentBase2 = this.wm.getDocumentBase();
                project = FileOwnerQuery.getOwner(documentBase2);
                if (projectprop != null) {
                    EditableProperties ep = WicketFrameworkProvider.loadProperties(projectprop);
                    ep.setProperty("client.urlPart", WicketFrameworkProvider.this.panel.getURLPattern().replace("/*", ""));
                    WicketFrameworkProvider.storeProperties(projectprop, ep);
                }
                if ((openJavaHomePage = (OpenCookie)(javaHomePage = DataObject.find((FileObject)fileToOpen)).getCookie(OpenCookie.class)) != null) {
                    openJavaHomePage.open();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /**
     * This method replaces deprecated method in WebModule.
     *
     * @return source roots.
     */
    private FileObject[] getJavaSources(WebModule webModule) {
        FileObject docBase = webModule.getDocumentBase();
        Project project = FileOwnerQuery.getOwner(docBase);
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroups = sources.getSourceGroups("java");
        FileObject[] javaSources = new FileObject[sourceGroups.length];
        for (int i = 0; i < sourceGroups.length; i++) {
            javaSources[i] = sourceGroups[i].getRootFolder();
        }
        return javaSources;
    }
}
