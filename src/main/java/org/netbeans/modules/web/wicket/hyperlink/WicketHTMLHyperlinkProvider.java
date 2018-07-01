/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.hyperlink;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.wicket.JavaForMarkupQuery;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tim Boudreau
 */
public class WicketHTMLHyperlinkProvider implements HyperlinkProvider {

    private static final ErrorManager ERR = ErrorManager.getDefault().getInstance("org.netbeans.modules.web.wicket.hyperlink");
    private static final String WICKET_IDENTIFIER = "wicket:id";

    private Reference<Document> lastDocument = null;
    private int startOffset;
    private int endOffset;
    private String identifier;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean isHyperlinkPoint(Document doc, int offset) {
        block16:
        {
            ERR.log("isHyperlinkPoint() offset: " + offset);
            if (!(doc instanceof BaseDocument)) {
                return false;
            }
            BaseDocument bdoc = (BaseDocument)doc;
            JTextComponent target = Utilities.getFocusedComponent();
            if (target == null || target.getDocument() != doc) {
                return false;
            }
            bdoc.readLock();
            try {
                TokenHierarchy hi = TokenHierarchy.get((Document)doc);
                TokenSequence ts = hi.tokenSequence(HTMLTokenId.language());
                ts.move(offset);
                ts.moveNext();
                Token tok = ts.token();
                if (tok == null) {
                    break block16;
                }
                int tokOffset = ts.offset();
                switch ((HTMLTokenId)tok.id()) {
                    case VALUE: {
                        boolean operatorFound = false;
                        block13:
                        while (ts.movePrevious()) {
                            Token prev = ts.token();
                            switch ((HTMLTokenId)prev.id()) {
                                case ARGUMENT: {
                                    if (!operatorFound || !WICKET_IDENTIFIER.equals(prev.text().toString())) {
                                        break;
                                    }
                                    this.setLastDocument(doc);
                                    this.startOffset = tokOffset;
                                    this.endOffset = this.startOffset + tok.text().length();
                                    boolean bl = true;
                                    return bl;
                                }
                                case OPERATOR: {
                                    operatorFound = true;
                                    break;
                                }
                                case EOL:
                                case ERROR:
                                case WS: {
                                    break;
                                }
                                default: {
                                    boolean bl = false;
                                    return bl;
                                }
                            }
                        }
                        break;
                    }
                    default: {
                        boolean prev = false;
                        return prev;
                    }
                }
            } finally {
                bdoc.readUnlock();
            }
        }
        return false;
    }

    private Document getLastDocument() {
        return this.lastDocument == null ? null : this.lastDocument.get();
    }

    private void setLastDocument(Document doc) {
        this.lastDocument = new WeakReference<>(doc);
    }

    @Override
    public int[] getHyperlinkSpan(Document doc, int offset) {
        if (!(doc instanceof BaseDocument)) {
            return null;
        }
        BaseDocument bdoc = (BaseDocument)doc;
        JTextComponent target = Utilities.getFocusedComponent();
        Document last = this.getLastDocument();
        if (target == null || last != bdoc) {
            return null;
        }
        return new int[]{this.startOffset, this.endOffset};
    }

    @Override
    public void performClickAction(Document doc, int offset) {
        if (!(doc instanceof BaseDocument)) {
            return;
        }
        BaseDocument bdoc = (BaseDocument)doc;
        JTextComponent target = Utilities.getFocusedComponent();
        Document last = this.getLastDocument();
        if (target == null || last != bdoc) {
            return;
        }
        OpenJavaClassThread run = new OpenJavaClassThread(bdoc);
        RequestProcessor.getDefault().post((Runnable)run);
    }

    private class OpenJavaClassThread implements Runnable {

        private final BaseDocument doc;

        public OpenJavaClassThread(BaseDocument doc) {
            this.doc = doc;
        }

        @Override
        public void run() {
            FileObject fo = NbEditorUtilities.getFileObject((Document)this.doc);
            FileObject foJava = JavaForMarkupQuery.find(fo);
            try {
                DataObject dObject = DataObject.find((FileObject)foJava);
                final EditorCookie.Observable ec = (EditorCookie.Observable)dObject.getLookup().lookup(EditorCookie.Observable.class);
                if (ec != null) {
                    Utilities.runInEventDispatchThread((Runnable)new Runnable() {

                        @Override
                        public void run() {
                            JEditorPane[] panes = ec.getOpenedPanes();
                            if (panes != null && panes.length > 0) {
                                OpenJavaClassThread.this.setPosition(panes[0]);
                            } else {
                                ec.addPropertyChangeListener(new PropertyChangeListener() {

                                    @Override
                                    public void propertyChange(PropertyChangeEvent evt) {
                                        if ("openedPanes".equals(evt.getPropertyName())) {
                                            JEditorPane[] panes = ec.getOpenedPanes();
                                            if (panes != null && panes.length > 0) {
                                                OpenJavaClassThread.this.setPosition(panes[0]);
                                            }
                                            ec.removePropertyChangeListener((PropertyChangeListener)this);
                                        }
                                    }
                                });
                            }
                            ec.open();
                        }

                    });
                }
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace((Throwable)ex);
            }
        }

        private void setPosition(JEditorPane pane) {
            try {
                String text = pane.getDocument().getText(0, pane.getDocument().getLength() - 1);
                int index = text.indexOf("\"" + WicketHTMLHyperlinkProvider.this.identifier + "\"");
                if (index > 0) {
                    pane.setCaretPosition(index);
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace((Throwable)ex);
            }
        }

    }

}
