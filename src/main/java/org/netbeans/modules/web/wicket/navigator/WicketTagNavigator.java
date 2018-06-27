/*
 * Decompiled with CFR 0_130.
 * 
 * Could not load the following classes:
 *  org.netbeans.spi.navigator.NavigatorPanel
 *  org.openide.cookies.EditorCookie
 *  org.openide.filesystems.FileObject
 *  org.openide.loaders.DataObject
 *  org.openide.util.Exceptions
 *  org.openide.util.Lookup
 *  org.openide.util.Lookup$Result
 *  org.openide.util.LookupEvent
 *  org.openide.util.LookupListener
 *  org.openide.util.NbBundle
 *  org.openide.windows.TopComponent
 */
package org.netbeans.modules.web.wicket.navigator;

import java.awt.BorderLayout;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.StyledDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.web.wicket.tree.HtmlTreeBuilder;
import org.netbeans.modules.web.wicket.tree.MarkupContainerTree;
import org.netbeans.modules.web.wicket.tree.Node;
import org.netbeans.modules.web.wicket.util.WicketSupportConstants;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

@NavigatorPanel.Registration(
        mimeType = WicketSupportConstants.MIME_TYPE_HTML,
        displayName = "#LBL_NAME"
)
public class WicketTagNavigator implements NavigatorPanel, LookupListener {

    private Lookup ctx = Lookup.EMPTY;
    Lookup.Result<DataObject> res;
    Reference<Pnl> tree;

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(WicketTagNavigator.class, "LBL_NAME");
    }

    @Override
    public String getDisplayHint() {
        return NbBundle.getMessage(WicketTagNavigator.class, "LBL_HINT");
    }

    @Override
    public JComponent getComponent() {
        Pnl pnl = this.getTree(true);
        this.createModel(this.ctx, pnl);
        return pnl;
    }

    @Override
    public void panelActivated(Lookup context) {
        this.ctx = context;
        this.res = context.lookupResult(DataObject.class);
        this.res.addLookupListener((LookupListener)this);
        this.res.allInstances();
        this.resultChanged(null);
    }

    @Override
    public void panelDeactivated() {
        if (this.res != null) {
            this.res.removeLookupListener((LookupListener)this);
        }
        this.res = null;
        this.ctx = Lookup.EMPTY;
        Pnl pnl = this.getTree(false);
        if (pnl != null) {
            pnl.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()), null);
        }
    }

    @Override
    public void resultChanged(LookupEvent e) {
        Pnl pnl = this.getTree(false);
        if (pnl != null) {
            this.createModel(this.ctx, pnl);
        }
    }

    @Override
    public Lookup getLookup() {
        return this.ctx;
    }

    private Pnl getTree(boolean create) {
        Pnl result = null;
        if (this.tree != null) {
            result = this.tree.get();
        }
        if (result == null && create) {
            result = new Pnl();
            this.tree = new WeakReference<>(result);
        }
        return result;
    }

    private void createModel(Lookup context, Pnl pnl) {
        DataObject dob = (DataObject)context.lookup(DataObject.class);
        if (dob == null) {
            return;
        }
        FileObject fob = dob.getPrimaryFile();
        if (fob != null) {
            try {
                EditorCookie ck = (EditorCookie)dob.getLookup().lookup(EditorCookie.class);
                if (ck != null) {
                    StyledDocument doc = ck.openDocument();
                    HtmlTreeBuilder builder = new HtmlTreeBuilder(doc);
                    MarkupContainerTree<String> tagTree = builder.getTree();
                    pnl.setModel(tagTree.toTreeModel(), fob);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace((Throwable)ex);
                pnl.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()), fob);
            }
        }
    }

    private void addNodes(DefaultMutableTreeNode node, Node<String> child) {
        DefaultMutableTreeNode nd = new DefaultMutableTreeNode(child);
        node.add(nd);
        List<Node<String>> l = child.getChildren();
        Collections.sort(l);
        for (Node<String> ch : l) {
            this.addNodes(nd, ch);
        }
    }

    private static final class Pnl
            extends JPanel
            implements TreeSelectionListener {

        private final JTree jt = new JTree(new DefaultTreeModel(new DefaultMutableTreeNode()));
        private FileObject file;
        boolean inSetModel;
        Boolean prev;

        private Pnl() {
            this.setLayout(new BorderLayout());
            JScrollPane pne = new JScrollPane(this.jt);
            this.add(pne);
            Border border = BorderFactory.createEmptyBorder();
            pne.setViewportBorder(border);
            pne.setBorder(border);
            this.jt.addTreeSelectionListener(this);
            this.jt.setRootVisible(false);
            this.jt.getSelectionModel().setSelectionMode(1);
        }

        public void setModel(TreeModel mdl, FileObject file) {
            this.inSetModel = true;
            this.file = file;
            this.jt.setModel(mdl);
            for (int i = 0; i < this.jt.getRowCount(); ++i) {
                this.jt.expandRow(i);
            }
            this.inSetModel = false;
        }

        @Override
        public void valueChanged(TreeSelectionEvent e) {
            DefaultMutableTreeNode node;
            Object o;
            if (this.inSetModel) {
                return;
            }
            TreePath path = e.getPath();
            if (path != null && (o = (node = (DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject()) instanceof Node) {
                Node nd = (Node)o;
                this.selected(nd);
            }
        }

        private void selected(Node nd) {
            if (this.file == null || !this.file.isValid()) {
                return;
            }
            try {
                DataObject ob = DataObject.find((FileObject)this.file);
                EditorCookie ec = (EditorCookie)ob.getLookup().lookup(EditorCookie.class);
                if (ec != null) {
                    JEditorPane[] t = ec.getOpenedPanes();
                    if (t == null) {
                        System.err.println("Got null opened panes for " + this.file.getPath());
                        return;
                    }
                    JEditorPane editor = ec.getOpenedPanes()[0];
                    editor.setCaretPosition(nd.getOffset());
                    TopComponent tc = (TopComponent)SwingUtilities.getAncestorOfClass(TopComponent.class, editor);
                    if (tc != null) {
                        tc.requestActive();
                    }
                    editor.requestFocus();
                }
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace((Throwable)ex);
            }
        }

        @Override
        public void addNotify() {
            super.addNotify();
            TopComponent tc = (TopComponent)SwingUtilities.getAncestorOfClass(TopComponent.class, this);
            if (tc != null) {
                this.prev = (Boolean)tc.getClientProperty((Object)"dontActivate");
                tc.putClientProperty((Object)"dontActivate", (Object)Boolean.TRUE);
            }
        }

        @Override
        public void removeNotify() {
            TopComponent tc = (TopComponent)SwingUtilities.getAncestorOfClass(TopComponent.class, this);
            if (tc != null) {
                tc.putClientProperty((Object)"dontActivate", (Object)this.prev);
            }
            super.removeNotify();
        }
    }

}
