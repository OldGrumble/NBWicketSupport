/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.tree.util;

import org.netbeans.modules.web.wicket.tree.finders.TreeCallback;
import org.netbeans.modules.web.wicket.tree.JavaTreeBuilder;
import org.netbeans.modules.web.wicket.tree.HtmlTreeBuilder;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.List;
import javax.swing.text.StyledDocument;
import org.netbeans.api.wicket.MarkupForJavaQuery;
import org.netbeans.modules.web.wicket.tree.MarkupContainerTree;
import org.netbeans.modules.web.wicket.util.WicketSupportConstants;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 *
 * @author Tim Boudreau
 */
@ActionID(id = "org.netbeans.modules.web.wicket.tree.SomeAction", category = "BpelNodes")
@ActionRegistration(displayName = "SomeAction", lazy = false)
@ActionReferences(value = {
    @ActionReference(path = "Editors/" + WicketSupportConstants.MIME_TYPE_JAVA + "/Popup"),
    @ActionReference(path = "Loaders/" + WicketSupportConstants.MIME_TYPE_JAVA + "/Actions")
})
public final class TestWicketParsingAction extends CookieAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        DataObject dataObject = (DataObject)activatedNodes[0].getLookup().lookup(DataObject.class);
        FileObject fob = dataObject.getPrimaryFile();
        JavaTreeBuilder jb = new JavaTreeBuilder(fob);
        jb.analyze(new CB(fob));
    }

    @Override
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(TestWicketParsingAction.class, (String)"CTL_SomeAction");
    }

    @Override
    protected Class[] cookieClasses() {
        return new Class[]{DataObject.class};
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.putValue("noIconInMenu", (Object)Boolean.TRUE);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    private class CB implements TreeCallback, Runnable {

        final FileObject javaFile;
        MarkupContainerTree<String> javaTree;
        MarkupContainerTree<String> htmlTree;

        CB(FileObject obj) {
            this.javaFile = obj;
        }

        @Override
        public void setTree(MarkupContainerTree<String> tree) {
            if (tree == null) {
                return;
            }
            this.javaTree = tree;
            FileObject htmlFile = MarkupForJavaQuery.find(this.javaFile);
            if (htmlFile == null || !htmlFile.isValid() || !this.javaFile.isValid()) {
                System.err.println("File not valid, die");
                return;
            }
            try {
                DataObject dob = DataObject.find((FileObject)htmlFile);
                EditorCookie ck = (EditorCookie)dob.getLookup().lookup(EditorCookie.class);
                if (ck != null) {
                    StyledDocument doc = ck.openDocument();
                    System.err.println("Got document " + doc);
                    if (htmlFile != null) {
                        HtmlTreeBuilder builder = new HtmlTreeBuilder(doc);
                        this.htmlTree = builder.getTree();
                    }
                } else {
                    System.err.println("Editor cookie null");
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace((Throwable)ex);
            }
            if (this.javaTree != null && this.htmlTree != null) {
                System.err.println("Running diff on event queue");
                EventQueue.invokeLater(this);
            }
        }

        @Override
        public void run() {
            System.err.println("Diff running");
            TreeDiff diff = new TreeDiff(this.htmlTree, this.javaTree);
            List<TreeDiff.Problem> l = diff.getProblems();
            if (l.isEmpty()) {
                System.err.println("No problems - html and java match");
            } else {
                for (TreeDiff.Problem p : l) {
                    System.err.println(p);
                }
            }
        }
    }
}
