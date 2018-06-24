/*
 * Decompiled with CFR 0_130.
 * 
 * Could not load the following classes:
 *  org.netbeans.api.project.FileOwnerQuery
 *  org.netbeans.api.project.Project
 *  org.netbeans.lib.editor.util.swing.DocumentListenerPriority
 *  org.netbeans.lib.editor.util.swing.DocumentUtilities
 *  org.netbeans.modules.editor.NbEditorUtilities
 *  org.openide.cookies.EditorCookie
 *  org.openide.cookies.LineCookie
 *  org.openide.filesystems.FileObject
 *  org.openide.loaders.DataObject
 *  org.openide.loaders.DataObjectNotFoundException
 *  org.openide.nodes.Node
 *  org.openide.nodes.Node$Cookie
 *  org.openide.text.Annotatable
 *  org.openide.text.Annotation
 *  org.openide.text.AnnotationProvider
 *  org.openide.text.Line
 *  org.openide.text.Line$Set
 *  org.openide.util.Exceptions
 *  org.openide.util.Lookup
 *  org.openide.util.RequestProcessor
 *  org.openide.util.RequestProcessor$Task
 */
package org.netbeans.modules.web.wicket.verification;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.wicket.JavaForMarkupQuery;
import org.netbeans.api.wicket.MarkupForJavaQuery;
import org.netbeans.api.wicket.WicketProjectQuery;
import org.netbeans.lib.editor.util.swing.DocumentListenerPriority;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.wicket.tree.HtmlTreeBuilder;
import org.netbeans.modules.web.wicket.tree.JavaTreeBuilder;
import org.netbeans.modules.web.wicket.tree.MarkupContainerTree;
import org.netbeans.modules.web.wicket.tree.TreeDiff;
import org.netbeans.modules.web.wicket.util.WicketSupportConstants;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Annotatable;
import org.openide.text.Annotation;
import org.openide.text.AnnotationProvider;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = AnnotationProvider.class)
public final class WicketProblemAnnotationProvider implements AnnotationProvider {

    private static final Logger ERR = Logger.getLogger(WicketProblemAnnotationProvider.class.getPackage().getName());
    private static final String WICKET_PROBLEM_SCANNING_RESULT = "wicket.problem_scaning.result";
    private static final RequestProcessor REQUEST_PROCESSOR;

    private final WeakHashMap<FileObject, Collection<Annotation>> file2annotation = new WeakHashMap();
    private final List<ListenerCacheEntry> cache = new LinkedList<>();

    public WicketProblemAnnotationProvider() {
        ERR.log(Level.FINEST, "AnnotationProvider created");
    }

    @Override
    public void annotate(Line.Set lines, Lookup context) {
        DataObject dataObj = (DataObject)context.lookup(DataObject.class);
        if (!dataObj.isValid()) {
            ERR.log(Level.FINE, "Invalid data object to scan: {0}", (Object)dataObj);
            return;
        }
        FileObject file = dataObj.getPrimaryFile();
        if (!WicketProblemAnnotationProvider.isJavaFile(file) && !WicketProblemAnnotationProvider.isHtmlFile(file)) {
            return;
        }
        Project p = FileOwnerQuery.getOwner((FileObject)file);
        if (p == null || !WicketProjectQuery.isWicket(p)) {
            ERR.log(Level.FINEST, "Not a wicket project: {0}", file.getPath());
        }
        ERR.log(Level.FINE, "Initial annotate of {0} by {1}", new Object[]{file.getPath(), this});
        this.annotate(lines, file);
    }

    private static boolean isJavaFile(FileObject file) {
        return WicketSupportConstants.MIME_TYPE_JAVA.equals(file.getMIMEType());
    }

    private static boolean isHtmlFile(FileObject file) {
        String mime = file.getMIMEType();
        return WicketSupportConstants.MIME_TYPE_HTML.equals(mime) || WicketSupportConstants.MIME_TYPE_XHTML.equals(mime);
    }

    private void annotate(Line.Set lines, FileObject file) {
        FileObject html;
        FileObject java;
        Line.Set htmlLines = null;
        Line.Set javaLines = null;
        if (WicketProblemAnnotationProvider.isJavaFile(file)) {
            java = file;
            html = MarkupForJavaQuery.find(java);
            javaLines = lines;
        } else if (WicketProblemAnnotationProvider.isHtmlFile(file)) {
            html = file;
            java = JavaForMarkupQuery.find(html);
            htmlLines = lines;
        } else {
            ERR.log(Level.FINE, "Unknown MIME type {0}", file.getMIMEType());
            return;
        }
        this.annotate(htmlLines, javaLines, html, java);
    }

    private static Line.Set linesFor(FileObject file) {
        if (file == null) {
            return null;
        }
        try {
            DataObject dob = DataObject.find((FileObject)file);
            LineCookie ck = (LineCookie)dob.getLookup().lookup(LineCookie.class);
            return ck == null ? null : ck.getLineSet();
        } catch (IOException ioe) {
            ERR.log(Level.WARNING, null, ioe);
            return null;
        }
    }

    private void annotate(Line.Set htmlLines, Line.Set javaLines, FileObject html, FileObject java) {
        if (html == null && java == null) {
            return;
        }
        if (html == null && java != null) {
            html = MarkupForJavaQuery.find(java);
        } else if (java != null && html == null) {
            java = JavaForMarkupQuery.find(html);
        }
        if (htmlLines == null) {
            htmlLines = WicketProblemAnnotationProvider.linesFor(html);
        }
        if (javaLines == null) {
            javaLines = WicketProblemAnnotationProvider.linesFor(java);
        }
        if (html == null || java == null) {
            ERR.log(Level.FINE, "Missing corresponding file: {0} : {1}", new Object[]{(Object)html, (Object)java});
            this.clearAnnotations(java);
            this.clearAnnotations(html);
            return;
        }
        if (htmlLines == null || javaLines == null) {
            ERR.log(Level.FINE, "Missing lines object or LineCookie in {0}", htmlLines == null ? html.getPath() : java.getPath());
            this.clearAnnotations(java);
            this.clearAnnotations(html);
            return;
        }
        Document htmlDoc = WicketProblemAnnotationProvider.documentFor(html);
        Document javaDoc = WicketProblemAnnotationProvider.documentFor(java);
        if (htmlDoc == null || javaDoc == null) {
            ERR.log(Level.FINE, "Either exception thrown or no EditorCookie for {0}:{1}", new Object[]{(Object)html, (Object)java});
            this.clearAnnotations(java);
            this.clearAnnotations(html);
        } else {
            ERR.log(Level.FINEST, "Beginning to listen on documents for {0} and {1} to provide annotations", new Object[]{html.getPath(), java.getPath()});
            this.startListeningTo(html, java, htmlDoc, javaDoc);
            this.annotate(htmlLines, javaLines, htmlDoc, javaDoc, html, java);
        }
    }

    private void annotate(Line.Set htmlLines, Line.Set javaLines, Document htmlDoc, Document javaDoc, FileObject html, FileObject java) {
        JavaTreeBuilder jbuilder = new JavaTreeBuilder(javaDoc);
        HtmlTreeBuilder hbuilder = new HtmlTreeBuilder(htmlDoc);
        TCB tcb = new TCB();
        jbuilder.analyze(tcb, null, true);
        MarkupContainerTree jtree = tcb.tree;
        MarkupContainerTree<String> htree = hbuilder.getTree();
        if (jtree == null || htree == null) {
            ERR.log(Level.FINE, "Missing markup container tree for {0}", jtree == null ? new StringBuilder().append("java file ").append(jtree).toString() : new StringBuilder().append("html file ").append(htree).toString());
            this.clearAnnotations(java);
            this.clearAnnotations(html);
            return;
        }
        this.clearAnnotations(java);
        this.clearAnnotations(html);
        this.annotate(htmlLines, javaLines, htmlDoc, javaDoc, htree, jtree, html, java);
    }

    private void clearAnnotations(FileObject file) {
        Collection<Annotation> old = this.file2annotation.remove(file);
        if (old != null) {
            for (Annotation a : old) {
                a.detach();
            }
        }
    }

    private void annotate(Line.Set htmlLines, Line.Set javaLines, Document htmlDoc, Document javaDoc, MarkupContainerTree<String> htree, MarkupContainerTree<String> jtree, FileObject html, FileObject java) {
        ArrayList<Annotation> htmlAnns = new ArrayList<>();
        ArrayList<Annotation> javaAnns = new ArrayList<>();
        TreeDiff diff = new TreeDiff(htree, jtree);
        List<TreeDiff.Problem> problems = diff.getProblems();
        block8:
        for (TreeDiff.Problem problem : problems) {
            String problemText = (Object)((Object)problem.getKind()) + ":" + problem;
            MarkupContainerTree.Node<String> javaNode;
            MarkupContainerTree.Node<String> node;
            int offset;
            switch (problem.getKind()) {
                case HTML_NODE_MISSING: {
                    node = problem.getProblemParentHtmlNode();
                    offset = node.getOffset();
                    htmlAnns.add(this.annotate(htmlDoc, htmlLines, offset, problemText, html));
                    continue block8;
                }
                case HTML_NODE_ADDED:
                case DUPLICATE_HTML_IDS: {
                    node = problem.getProblemHtmlNode();
                    offset = node.getOffset();
                    htmlAnns.add(this.annotate(htmlDoc, htmlLines, offset, problemText, html));
                    continue block8;
                }
                case DIFFERENT_IDS: {
                    MarkupContainerTree.Node<String> htmlNode = problem.getProblemHtmlNode();
                    offset = htmlNode.getOffset();
                    htmlAnns.add(this.annotate(htmlDoc, htmlLines, offset, problemText, html));
                    MarkupContainerTree.Node<String> javaNode2 = problem.getProblemJavaNode();
                    offset = javaNode2.getOffset();
                    javaAnns.add(this.annotate(javaDoc, javaLines, offset, problemText, java));
                    continue block8;
                }
                case JAVA_NODE_ADDED: {
                    javaNode = problem.getProblemJavaNode();
                    offset = javaNode.getOffset();
                    javaAnns.add(this.annotate(javaDoc, javaLines, offset, problemText, java));
                    continue block8;
                }
                case JAVA_NODE_MISSING: {
                    javaNode = problem.getProblemParentHtmlNode();
                    offset = javaNode.getOffset();
                    javaAnns.add(this.annotate(javaDoc, javaLines, offset, problemText, java));
                    continue block8;
                }
                case DUPLICATE_JAVA_IDS: {
                    javaNode = problem.getProblemParentJavaNode();
                    offset = javaNode.getOffset();
                    javaAnns.add(this.annotate(javaDoc, javaLines, offset, problemText, java));
                    continue block8;
                }
            }
            throw new AssertionError((Object)problem.getKind().name());
        }
        if (!htmlAnns.isEmpty()) {
            this.file2annotation.put(html, htmlAnns);
        }
        if (!javaAnns.isEmpty()) {
            this.file2annotation.put(java, javaAnns);
        }
    }

    private Annotation annotate(Document doc, Line.Set lines, int offset, String problem, FileObject source) {
        Line line = NbEditorUtilities.getLine((Document)doc, (int)offset, (boolean)false);
        WicketVerificationProblemMark ann = new WicketVerificationProblemMark(problem);
        ann.attach((Annotatable)line);
        return ann;
    }

    private static Document documentFor(FileObject file) {
        try {
            DataObject ob = DataObject.find((FileObject)file);
            EditorCookie ck = (EditorCookie)ob.getLookup().lookup(EditorCookie.class);
            if (ck != null) {
                return ck.openDocument();
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace((Throwable)ioe);
        }
        return null;
    }

    private synchronized boolean startListeningTo(FileObject html, FileObject java, Document htmlDoc, Document javaDoc) {
        boolean notResult = false;
        Iterator<ListenerCacheEntry> i = this.cache.iterator();
        while (i.hasNext()) {
            ListenerCacheEntry l = i.next();
            if (l.isDead()) {
                if (ERR.isLoggable(Level.FINE)) {
                    ERR.log(Level.FINE, "Disposing {0} - both files it listened to are gone", l);
                }
                i.remove();
                continue;
            }
            notResult |= l.isListeningTo(java) || l.isListeningTo(html);
        }
        if (!notResult) {
            ListenerCacheEntry entry = new ListenerCacheEntry(html, java, htmlDoc, javaDoc);
            this.cache.add(entry);
        }
        return !notResult;
    }

    static /* synthetic */ RequestProcessor access$600() {
        return REQUEST_PROCESSOR;
    }

    static {
        ERR.setLevel(Level.FINEST);
        REQUEST_PROCESSOR = new RequestProcessor("Wicket Java and Html Annotation Parser", 2);
    }

    private class ListenerCacheEntry {

        private Reference<DataObject> htmld;
        private Reference<DataObject> javad;
        private Reference<ScanningDocumentListener> htmll;
        private Reference<ScanningDocumentListener> javal;
        private String refString;

        ListenerCacheEntry(FileObject html, FileObject java, Document htmlDoc, Document javaDoc) {
            try {
                DataObject htmldob = DataObject.find((FileObject)html);
                DataObject javadob = DataObject.find((FileObject)java);
                this.setHtmlFile(htmldob, htmlDoc);
                this.setJavaFile(javadob, javaDoc);
                if (ERR.isLoggable(Level.FINE)) {
                    this.refString = html.getPath() + " and " + java.getPath();
                }
            } catch (IOException ioe) {
                throw new IllegalStateException(ioe);
            }
        }

        @Override
        public String toString() {
            return this.refString == null ? super.toString() : super.toString() + '[' + this.refString + ']';
        }

        private void setHtmlFile(DataObject html, Document htmlDoc) {
            assert (this.htmld == null || this.htmld.get() == null);
            this.htmld = new WeakReference<>(html);
            if (htmlDoc == null) {
                htmlDoc = WicketProblemAnnotationProvider.documentFor(html.getPrimaryFile());
            }
            ScanningDocumentListener l = this.getListener(false);
            l.attachTo(html, htmlDoc, false);
            ERR.log(Level.FINER, "Attached listener to {0}", html.getPrimaryFile().getPath());
        }

        private void setJavaFile(DataObject java, Document javaDoc) {
            assert (this.javad == null || this.javad.get() == null);
            this.javad = new WeakReference<>(java);
            if (javaDoc == null) {
                javaDoc = WicketProblemAnnotationProvider.documentFor(java.getPrimaryFile());
            }
            ScanningDocumentListener l = this.getListener(true);
            l.attachTo(java, javaDoc, true);
            ERR.log(Level.FINER, "Attached listener to {0}", java.getPrimaryFile().getPath());
        }

        private ScanningDocumentListener getListener(boolean java) {
            ScanningDocumentListener result;
            Reference<ScanningDocumentListener> reference = java ? javal : htmll;
            if (reference == null) {
                result = new ScanningDocumentListener();
                if (java) {
                    javal = new WeakReference<>(result);
                } else {
                    htmll = new WeakReference<>(result);
                }
            } else {
                result = reference.get();
            }
            return result;
        }

        boolean isListeningTo(FileObject file) {
            try {
                boolean result;
                DataObject dob = DataObject.find((FileObject)file);
                DataObject hdob = this.htmld == null ? null : this.htmld.get();
                DataObject jdob = this.javad == null ? null : this.javad.get();
                boolean bl = result = dob == hdob || dob == jdob;
                if (!result && hdob == null != (jdob == null)) {
                    if (hdob == null && WicketSupportConstants.MIME_TYPE_HTML.equals(dob.getPrimaryFile().getMIMEType())) {
                        if (file.equals((Object)JavaForMarkupQuery.find(jdob.getPrimaryFile()))) {
                            ERR.log(Level.FINE, "Reattaching to lost java file {0}", file.getPath());
                            this.setJavaFile(dob, null);
                            result = true;
                        }

                    } else if (jdob == null && WicketSupportConstants.MIME_TYPE_JAVA.equals(dob.getPrimaryFile().getMIMEType()) && file.equals((Object)MarkupForJavaQuery.find(hdob.getPrimaryFile()))) {
                        ERR.log(Level.FINE, "Reattaching to lost html file {0}", file.getPath());
                        this.setHtmlFile(dob, null);
                        result = true;
                    }
                }
                ERR.log(Level.FINEST, "Check listening to {0} result {1}", new Object[]{file.getPath(), result});
                return result;
            } catch (DataObjectNotFoundException e) {
                ERR.log(Level.WARNING, null, (Throwable)e);
                return false;
            }
        }

        private boolean isDead() {
            boolean result;
            DataObject hdob = this.htmld == null ? null : this.htmld.get();
            DataObject jdob = this.javad == null ? null : this.javad.get();
            boolean bl = result = hdob == null && jdob == null;
            if (!(result || hdob != null && hdob.isValid() || jdob != null && jdob.isValid())) {
                result = true;
            } else if (hdob == null != (jdob == null)) {
                FileObject otherFile;
                FileObject fileObject = otherFile = hdob == null ? jdob.getPrimaryFile() : hdob.getPrimaryFile();
                if (otherFile.isValid()) {
                    result = false;
                    try {
                        FileObject nue;
                        if (WicketSupportConstants.MIME_TYPE_HTML.equals(otherFile.getMIMEType())) {
                            FileObject nue2 = JavaForMarkupQuery.find(otherFile);
                            if (nue2 != null) {
                                DataObject dob = DataObject.find((FileObject)nue2);
                                this.javad = new WeakReference<>(dob);
                            }
                        } else if (WicketSupportConstants.MIME_TYPE_JAVA.equals(otherFile.getMIMEType()) && (nue = MarkupForJavaQuery.find(otherFile)) != null) {
                            DataObject dob = DataObject.find((FileObject)nue);
                            this.htmld = new WeakReference<>(dob);
                        }
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace((Throwable)ioe);
                    }
                } else {
                    return true;
                }
            }
            return result;
        }
    }

    private class ScanningDocumentListener
            implements DocumentListener,
            Runnable {

        private static final int AUTO_SCANNING_DELAY = 2000;
        private final RequestProcessor.Task parseTask = WicketProblemAnnotationProvider.access$600().create((Runnable)this);
        private boolean java;
        private Reference<DataObject> dobref;
        private String debugData;

        ScanningDocumentListener() {
        }

        @Override
        public void finalize() {
            ERR.log(Level.FINE, "{0} GARBAGE COLLECTED", this);
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            this.change(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            this.change(e);
        }

        @Override
        public String toString() {
            return super.toString() + (this.debugData == null ? null : this.debugData);
        }

        private void attachTo(Document document, boolean java) {
            DocumentUtilities.addDocumentListener((Document)document, (DocumentListener)this, (DocumentListenerPriority)DocumentListenerPriority.AFTER_CARET_UPDATE);
            this.java = java;
            this.restartTimer();
        }

        private void attachTo(DataObject dob, Document document, boolean java) {
            this.dobref = new WeakReference<>(dob);
            if (document == null) {
                document = WicketProblemAnnotationProvider.documentFor(dob.getPrimaryFile());
            }
            ERR.log(Level.FINER, "{0} attach to {1}", new Object[]{this, dob.getPrimaryFile().getPath()});
            this.attachTo(document, java);
            if (ERR.isLoggable(Level.FINE)) {
                this.debugData = "Document listener for " + dob.getPrimaryFile().getPath() + " java=" + java;
            }
        }

        private void change(DocumentEvent e) {
            this.restartTimer();
        }

        private void restartTimer() {
            ERR.log(Level.FINEST, "restart timer on {0}", this);
            this.parseTask.schedule(2000);
        }

        @Override
        public void run() {
            DataObject dob = this.dobref.get();
            if (dob != null) {
                Line.Set set;
                boolean done = false;
                ERR.log(Level.FINE, "Reparse file " + dob.getPrimaryFile().getPath());
                LineCookie ck = (LineCookie)dob.getLookup().lookup(LineCookie.class);
                if (ck != null && (set = ck.getLineSet()) != null) {
                    WicketProblemAnnotationProvider.this.annotate(set, dob.getPrimaryFile());
                    done = true;
                }
                if (!done) {
                    WicketProblemAnnotationProvider.this.clearAnnotations(dob.getPrimaryFile());
                }
            }
        }
    }

    private static class TCB implements JavaTreeBuilder.TreeCallback {

        private MarkupContainerTree<String> tree;

        private TCB() {
        }

        @Override
        public void setTree(MarkupContainerTree<String> tree) {
            this.tree = tree;
        }
    }
}
