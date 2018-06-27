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
 *  org.openide.util.Mutex
 *  org.openide.util.NbBundle
 *  org.openide.windows.TopComponent
 */
package org.netbeans.modules.web.wicket.navigator;

import java.awt.BorderLayout;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
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
import org.netbeans.modules.web.wicket.tree.JavaTreeBuilder;
import org.netbeans.modules.web.wicket.tree.MarkupContainerTree;
import org.netbeans.modules.web.wicket.tree.Node;
import org.netbeans.modules.web.wicket.tree.finders.TreeCallback;
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
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

@NavigatorPanel.Registration(
        mimeType = WicketSupportConstants.MIME_TYPE_HTML,
        displayName = "#LBL_NAME_JAVA"
)
public class WicketJavaTagNavigator implements NavigatorPanel, LookupListener {

    private Lookup ctx = Lookup.EMPTY;
    Lookup.Result<DataObject> res;
    Reference<Pnl> tree;
    private CB currentCallback = null;

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(WicketJavaTagNavigator.class, "LBL_NAME_JAVA");
    }

    @Override
    public String getDisplayHint() {
        return NbBundle.getMessage(WicketJavaTagNavigator.class, "LBL_HINT_JAVA");
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
                DefaultTreeModel mdl = new DefaultTreeModel(new DefaultMutableTreeNode());
                pnl.setModel(mdl, fob);
                EditorCookie ck = (EditorCookie)dob.getLookup().lookup(EditorCookie.class);
                if (ck != null) {
                    StyledDocument doc = ck.openDocument();
                    new JavaTreeBuilder(doc).analyze(this.createCB(fob, pnl));
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private synchronized CB createCB(FileObject fob, Pnl pnl) {
        if (this.currentCallback != null) {
            this.currentCallback.cancelled = true;
        }
        this.currentCallback = new CB(fob, pnl);
        return this.currentCallback;
    }

    private class CB
            implements TreeCallback {

        volatile boolean cancelled = false;
        final FileObject fob;
        final Pnl pnl;

        CB(FileObject fob, Pnl pnl) {
            this.fob = fob;
            this.pnl = pnl;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void setTree(final MarkupContainerTree<String> tagTree) {
            try {
                if (tagTree == null) {
                    return;
                }
                if (this.cancelled || tagTree == null) {
                    return;
                }
                Mutex.EVENT.readAccess(new Runnable() {

                    @Override
                    public void run() {
                        TreeModel mdl = tagTree.toTreeModel();
                        CB.this.pnl.setModel(mdl, CB.this.fob);
                    }
                });
            } finally {
                this.cancelled = true;
                WicketJavaTagNavigator wicketJavaTagNavigator = WicketJavaTagNavigator.this;
                synchronized (wicketJavaTagNavigator) {
                    if (WicketJavaTagNavigator.this.currentCallback == this) {
                        WicketJavaTagNavigator.this.currentCallback = null;
                    }
                }
            }
        }

    }

    private static final class Pnl
            extends JPanel
            implements TreeSelectionListener {

        private final JTree jt = new JTree(new DefaultTreeModel(new DefaultMutableTreeNode()));
        private FileObject file;
        boolean inSetModel = false;
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
                Exceptions.printStackTrace(ex);
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
